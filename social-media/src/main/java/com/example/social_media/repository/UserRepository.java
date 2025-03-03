package com.example.social_media.repository;

import com.example.social_media.entity.User;
import com.google.cloud.firestore.Firestore;
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
    public void save(User user, String uid) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(uid).set(user).get();
    }

}
