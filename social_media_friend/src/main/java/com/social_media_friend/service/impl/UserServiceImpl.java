package com.social_media_friend.service.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.social_media_friend.dto.request.UpdateFollowCountsRequest;
import com.social_media_friend.dto.response.UserResponse;
import com.social_media_friend.entity.UserRelationship;
import com.social_media_friend.repository.UserRepository;
import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Firestore firestore;
    UserRepository userRepository;
    RestTemplate restTemplate;
    static String AUTH_SERVICE_URL = "http://localhost:8080/api/auth/users/";

    @Override
    public void followUser(String followerId, String followedId) {
        try {
            String relationshipId = followerId + "_" + followedId;
            DocumentReference relationshipRef = firestore.collection("relationships").document(relationshipId);
            UserRelationship relationship = UserRelationship.builder().id(relationshipId).followerId(followerId).followedId(followedId).followAt(Timestamp.now()).build();
            relationshipRef.set(relationship);

            updateAuthServiceUser(followerId, followedId, "increment");
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
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service", e);
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
    public List<String> getFollowers(String followedId, int page, int size) throws ExecutionException, InterruptedException {
        Query query = firestore.collection("relationships")
                .whereEqualTo("followedId", followedId)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page - 1) * size);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followerIds = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            followerIds.add(doc.getString("followerId"));
        }
        return followerIds;
    }

    @Override
    public List<String> getFollowing(String followerId, int page, int size) throws ExecutionException, InterruptedException {
        Query query = firestore.collection("relationships")
                .whereEqualTo("followerId", followerId)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page - 1) * size);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        List<String> followedIds = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            followedIds.add(doc.getString("followedId"));
        }
        return followedIds;
    }

    @Override
    public List<String> getFriends(String uid, int page, int size) throws ExecutionException, InterruptedException {
        Query followingQuery = firestore.collection("relationships")
                .whereEqualTo("followerId", uid);
        List<QueryDocumentSnapshot> followingDocs = followingQuery.get().get().getDocuments();
        List<String> followedIds = followingDocs.stream()
                .map(doc -> doc.getString("followedId"))
                .collect(Collectors.toList());

        if (followedIds.isEmpty())
            return Collections.emptyList();

        Query friendQuery = firestore.collection("relationships")
                .whereIn("followerId", followedIds)
                .whereEqualTo("followedId", uid)
                .orderBy("followAt", Query.Direction.DESCENDING)
                .limit(size)
                .offset((page-1)*size);
        List<QueryDocumentSnapshot> friendDocs = friendQuery.get().get().getDocuments();
        return friendDocs.stream()
                .map(doc -> doc.getString("followerId"))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(String followerId, String followedId) throws ExecutionException, InterruptedException {
        String relationshipId = followerId + "_" +followedId;
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
