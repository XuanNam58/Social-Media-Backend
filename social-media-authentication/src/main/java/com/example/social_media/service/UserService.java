package com.example.social_media.service;

import com.example.social_media.dto.response.UserFollowRes;
import com.example.social_media.entity.User;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface UserService {
    User getUserInfo(String token) throws FirebaseAuthException;
    boolean isUsernameExists(String username) throws ExecutionException, InterruptedException;
    void save(User user, String uid) throws ExecutionException, InterruptedException;
    void updateUserFollowers(String uid, List<String> followers) throws ExecutionException, InterruptedException;
    void updateUserFollowing(String uid, List<String> following) throws ExecutionException, InterruptedException;
    List<Map<String, Object>> searchUsers(String query) throws ExecutionException, InterruptedException;
    Map<String, Object> getUserByUsername(String username) throws ExecutionException, InterruptedException;
    String getUidByUsername(String username) throws ExecutionException, InterruptedException;
    List<UserFollowRes> getUsersByIds(List<String> ids);
}
