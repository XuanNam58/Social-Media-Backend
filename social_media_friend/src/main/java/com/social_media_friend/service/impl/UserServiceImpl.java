package com.social_media_friend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.social_media_friend.dto.request.UpdateFollowCountsRequest;
import com.social_media_friend.dto.request.UserFollowRequest;
import com.social_media_friend.dto.response.UserFollowResponse;
import com.social_media_friend.dto.response.UserResponse;
import com.social_media_friend.entity.UserRelationship;
import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    //    SimpMessagingTemplate messagingTemplate;
    KafkaTemplate<String, String> kafkaTemplate;

    static String AUTH_SERVICE_URL = "http://auth-service:8080/api/auth/users/";

    @Override
    public void followUser(UserFollowRequest request) {
        try {
            String relationshipId = request.getFollowerId() + "_" + request.getFollowedId();
            DocumentReference relationshipRef = firestore.collection("relationships").document(relationshipId);
            UserRelationship relationship = UserRelationship.builder()
                    .id(relationshipId)
                    .followerId(request.getFollowerId())
                    .followedId(request.getFollowedId())
                    .followAt(Timestamp.now())
                    .build();
            relationshipRef.set(relationship);

            updateAuthServiceUser(request.getFollowerId(), request.getFollowedId(), "increment");

           /* Invalidate cache
            xóa đi danh sách khi có người follow mới
            */
            deleteKeysByPattern("user:followers:" + request.getFollowedId() + ":*");
            deleteKeysByPattern("user:following:" + request.getFollowerId() + ":*");
            deleteKeysByPattern("user:friends:" + request.getFollowerId() + ":*");
            deleteKeysByPattern("user:friends:" + request.getFollowedId() + ":*");

            String reverseRelationshipId = request.getFollowedId() + "_" + request.getFollowerId();
            DocumentSnapshot reverseDoc = firestore.collection("relationships").document(reverseRelationshipId).get().get();

            Map<String, Object> message = new HashMap<>();
            message.put("senderUsername", request.getFollowerUsername());
            message.put("followReceiver", request.getFollowedUsername());
            message.put("type", "follow");
            message.put("isRead", false);
            message.put("postId", null);
            if (reverseDoc.exists()) {
                message.put("content", request.getFollowerFullname() + " started following you. "
                + "You and " + request.getFollowerFullname() + " are now friends");
            } else {
                message.put("content", request.getFollowerFullname() + " started following you");
            }

            ObjectMapper mapper = new ObjectMapper();
            kafkaTemplate.send("post-notifications", mapper.writeValueAsString(message));

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
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
                new ParameterizedTypeReference<List<UserFollowResponse>>() {
                }
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

    @Override
    public List<String> getFollowersWithUsername(String followedId) throws ExecutionException, InterruptedException {
        Query query = firestore.collection("relationships")
                .whereEqualTo("followedId", followedId);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followerIds = documents.stream()
                .map(doc -> doc.getString("followerId"))
                .collect(Collectors.toList());

        if (followerIds.isEmpty()) {
            return List.of();
        }

        List<String> followers = getUsernameList(followerIds);
        return followers;
    }

    @Override
    public List<String> getFollowingWithUsername(String followerId) throws ExecutionException, InterruptedException {
        Query query = firestore.collection("relationships")
                .whereEqualTo("followerId", followerId);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followedIds = documents.stream()
                .map(doc -> doc.getString("followedId"))
                .collect(Collectors.toList());

        if (followedIds.isEmpty()) {
            return List.of();
        }

        List<String> following = getUsernameList(followedIds);
        return following;
    }

    private List<String> getUsernameList(List<String> ids) {
        HttpEntity<List<String>> request = new HttpEntity<>(ids);
        ResponseEntity<List<String>> response = restTemplate.exchange(
                AUTH_SERVICE_URL + "get-usernames-by-ids",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<String>>() {
                }
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }


    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
