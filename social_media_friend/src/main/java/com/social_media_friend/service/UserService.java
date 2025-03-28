package com.social_media_friend.service;


import com.google.firebase.auth.FirebaseAuthException;
import com.social_media_friend.dto.response.UserResponse;
import com.social_media_friend.entity.User;

import java.util.concurrent.ExecutionException;

public interface UserService {
    void followUser(String token, String reqUserId, String followUserId) throws ExecutionException, InterruptedException;
    UserResponse getUserById(String token, String userId);
}
