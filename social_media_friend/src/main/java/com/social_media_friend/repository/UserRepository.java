package com.social_media_friend.repository;

import com.google.cloud.firestore.Firestore;
import com.social_media_friend.entity.UserRelationship;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Repository
public class UserRepository {
    Firestore firestore;
    public void saveUserRelationship(UserRelationship userRelationship) throws ExecutionException, InterruptedException {
        String docId = userRelationship.getFollowerId() + "_" + userRelationship.getFollowedId();
        firestore.collection("userRelationship").document(docId).set(userRelationship).get();
    }

    public void deleteUserRelationship(String followerId, String followedId) {
        String docId = followerId + "_" + followedId;
        firestore.collection("userRelationship").document(docId).delete();
    }

}
