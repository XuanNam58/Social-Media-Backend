package com.social_media_friend.service;


import com.social_media_friend.dto.response.UserResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {
    void followUser(String token, String followerId, String followedId) throws ExecutionException, InterruptedException;
    void unFollowUser(String token, String followerId, String followedId) throws ExecutionException, InterruptedException;
    UserResponse getUserById(String token, String userId);
    List<String> getMutualFollows(List<String> followers, List<String> following);
}
