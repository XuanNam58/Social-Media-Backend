package com.example.social_media.repository;

import com.example.social_media.entity.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Repository
public class UserRepository {
    Firestore firestore;
    public void save(User user, String uid) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(uid).set(user).get();
    }

    public void updateUserFollowers(String uid, List<String> followers) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("users").document(uid);
        documentReference.update("followers", followers).get();
    }

    public void updateUserFollowing(String uid, List<String> following) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("users").document(uid);
        documentReference.update("following", following).get();
    }
}
