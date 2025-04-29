package com.example.social_media.service;

import com.example.social_media.dto.response.UserFollowRes;
import com.example.social_media.entity.User;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface UserService {

    boolean isUsernameExists(String username) throws ExecutionException, InterruptedException;
    void save(User user, String uid) throws ExecutionException, InterruptedException;

    List<Map<String, Object>> searchUsers(String query) throws ExecutionException, InterruptedException;
    Map<String, Object> getUserByUsername(String username) throws ExecutionException, InterruptedException;
    String getUidByUsername(String username) throws ExecutionException, InterruptedException;
    List<UserFollowRes> getUsersByIds(List<String> ids);
    void incrementFollowerNum(String uid) throws ExecutionException, InterruptedException;
    void decrementFollowerNum(String uid) throws ExecutionException, InterruptedException;

    void incrementFollowingNum(String uid) throws ExecutionException, InterruptedException;
    void decrementFollowingNum(String uid) throws ExecutionException, InterruptedException;
}
