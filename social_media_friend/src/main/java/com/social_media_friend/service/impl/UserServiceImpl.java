package com.social_media_friend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.social_media_friend.dto.request.UpdateFollowCountsRequest;
import com.social_media_friend.dto.response.UserFollowResponse;
import com.social_media_friend.dto.response.UserResponse;
import com.social_media_friend.entity.UserRelationship;
import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Firestore firestore;
    RestTemplate restTemplate;
    RedisTemplate<String, Object> redisTemplate;
//    StringRedisTemplate stringRedisTemplate;
//    ObjectMapper objectMapper;
    static String AUTH_SERVICE_URL = "http://localhost:8080/api/auth/users/";

    @Override
    public void followUser(String followerId, String followedId) {
        try {
            String relationshipId = followerId + "_" + followedId;
            DocumentReference relationshipRef = firestore.collection("relationships").document(relationshipId);
            UserRelationship relationship = UserRelationship.builder()
                    .id(relationshipId)
                    .followerId(followerId)
                    .followedId(followedId)
                    .followAt(Timestamp.now())
                    .build();
            relationshipRef.set(relationship);

            updateAuthServiceUser(followerId, followedId, "increment");

           /* Invalidate cache
            xóa đi danh sách khi có người follow mới
            */
            deleteKeysByPattern("user:followers:" + followedId + ":*");
            deleteKeysByPattern("user:following:" + followerId + ":*");
            deleteKeysByPattern("user:friends:" + followerId + ":*");
            deleteKeysByPattern("user:friends:" + followedId + ":*");

//            stringRedisTemplate.convertAndSend("follow-events",
//                    "\"followerId\":\"" + followerId + "\",\"followedId\":\"" + followedId + "\"}");
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service", e);
        }
    }

    @Override
    public void unFollowUser(String followerId, String followedId) {
        try {
            String relationshipId = followerId + "_" + followedId;
            firestore.collection("relationships").document(relationshipId).delete();

            updateAuthServiceUser(followerId, followedId, "decrement");
            /* Invalidate cache
            xóa đi danh sách khi có người follow mới
            */
            deleteKeysByPattern("user:followers:" + followedId + ":*");
            deleteKeysByPattern("user:following:" + followerId + ":*");
            deleteKeysByPattern("user:friends:" + followerId + ":*");
            deleteKeysByPattern("user:friends:" + followedId + ":*");
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service", e);
        }
    }

    public void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }


    private void updateAuthServiceUser(String followerId, String followedId, String operation) {

        HttpEntity<UpdateFollowCountsRequest> entity = new HttpEntity<>(UpdateFollowCountsRequest.builder()
                .followerId(followerId)
                .followedId(followedId)
                .operation(operation)
                .build());

        ResponseEntity<String> response = restTemplate.exchange(
                AUTH_SERVICE_URL + "update-follow-counts",
                HttpMethod.POST,
                entity,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update follow counts in auth service");
        }
    }

    @Override
    public UserResponse getUserById(String token, String userId) {
        HttpHeaders headers = createHeaders(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(AUTH_SERVICE_URL + userId, HttpMethod.GET, entity, UserResponse.class);
        return (UserResponse) response.getBody();
    }

    @Override
    public List<UserFollowResponse> getFollowers(String followedId, int page, int size) throws ExecutionException, InterruptedException {
        String cacheKey = "user:followers:" + followedId + ":" + page;
        List<UserFollowResponse> cachedFollowers = (List<UserFollowResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedFollowers != null) {
            return cachedFollowers;
        }

        Query query = firestore.collection("relationships")
                .whereEqualTo("followedId", followedId)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page - 1) * size);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followerIds = documents.stream()
                .map(doc -> doc.getString("followerId"))
                .collect(Collectors.toList());

        if (followerIds.isEmpty()) {
            return List.of();
        }

        List<UserFollowResponse> followers = callAuthService(followerIds);
        redisTemplate.opsForValue().set(cacheKey, followers, 15, TimeUnit.MINUTES);
//        15 là thời gian sống của dữ liệu. Sau 15p sẽ tự động xóa
        return followers;
    }

    @Override
    public List<UserFollowResponse> getFollowing(String followerId, int page, int size) throws ExecutionException, InterruptedException {
        String cacheKey = "user:following:" + followerId + ":" + page;
        List<UserFollowResponse> cachedFollowing = (List<UserFollowResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedFollowing != null) {
            return cachedFollowing;
        }

        Query query = firestore.collection("relationships")
                .whereEqualTo("followerId", followerId)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page - 1) * size);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followedIds = documents.stream()
                .map(doc -> doc.getString("followedId"))
                .collect(Collectors.toList());

        if (followedIds.isEmpty()) {
            return List.of();
        }

        List<UserFollowResponse> following = callAuthService(followedIds);
        redisTemplate.opsForValue().set(cacheKey, following, 15, TimeUnit.MINUTES);
        return following;
    }

    @Override
    public List<UserFollowResponse> getFriends(String uid, int page, int size) throws ExecutionException, InterruptedException {
//        System.out.println("page: " + page + " size: " + size);
        String cacheKey = "user:friends:" + uid + ":" + page;
        List<UserFollowResponse> cachedFriends = (List<UserFollowResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedFriends != null)
            return cachedFriends;

        Query followingQuery = firestore.collection("relationships")
                .whereEqualTo("followerId", uid);
        List<QueryDocumentSnapshot> followingDocs = followingQuery.get().get().getDocuments();
        List<String> followedIds = followingDocs.stream()
                .map(doc -> doc.getString("followedId"))
                .collect(Collectors.toList());

        if (followedIds.isEmpty())
            return List.of();

        Query friendQuery = firestore.collection("relationships")
                .whereIn("followerId", followedIds)
                .whereEqualTo("followedId", uid)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page - 1) * size);
        List<QueryDocumentSnapshot> friendDocs = friendQuery.get().get().getDocuments();
        List<String> friendIds = friendDocs.stream()
                .map(doc -> doc.getString("followerId"))
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return List.of();
        }

        List<UserFollowResponse> friends = callAuthService(friendIds);
        redisTemplate.opsForValue().set(cacheKey, friends, 15, TimeUnit.MINUTES);
        return friends;
    }

    private List<UserFollowResponse> callAuthService(List<String> ids) {
        HttpEntity<List<String>> request = new HttpEntity<>(ids);
        ResponseEntity<List<UserFollowResponse>> response = restTemplate.exchange(
                AUTH_SERVICE_URL + "user-list",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<UserFollowResponse>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public boolean isFollowing(String followerId, String followedId) throws ExecutionException, InterruptedException {
        String relationshipId = followerId + "_" + followedId;
        DocumentReference relationshipRef = firestore.collection("relationships").document(relationshipId);
        DocumentSnapshot document = relationshipRef.get().get();
        return document.exists();
    }


    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
