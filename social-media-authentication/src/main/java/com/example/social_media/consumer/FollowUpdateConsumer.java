package com.example.social_media.consumer;

import com.example.social_media.dto.request.UpdateFollowCountsRequest;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FollowUpdateConsumer {
    Firestore firestore;
    public void processFollowUpdate(UpdateFollowCountsRequest update) throws Exception {
        DocumentReference followerDoc = firestore.collection("users").document(update.getFollowerId());
        DocumentReference followedDoc = firestore.collection("users").document(update.getFollowedId());

        firestore.runTransaction(transaction -> {
            if ("increment".equals(update.getOperation())) {
                transaction.update(followerDoc, "followingNum", FieldValue.increment(1));
                transaction.update(followedDoc, "followerNum", FieldValue.increment(1));
            } else if ("decrement".equals(update.getOperation())) {
                transaction.update(followerDoc, "followingNum", FieldValue.increment(-1));
                transaction.update(followedDoc, "followerNum", FieldValue.increment(-1));
            }
            return null;
        }).get();
    }
}
