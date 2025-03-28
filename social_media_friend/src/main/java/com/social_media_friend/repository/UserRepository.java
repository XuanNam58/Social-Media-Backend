package com.social_media_friend.repository;

import com.google.cloud.firestore.Firestore;
import com.social_media_friend.entity.User;
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
