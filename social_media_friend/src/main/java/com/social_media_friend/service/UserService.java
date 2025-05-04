package com.social_media_friend.service;


import com.social_media_friend.dto.response.UserResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {
    void followUser(String followerId, String followedId);

    void unFollowUser(String followerId, String followedId);

    UserResponse getUserById(String token, String userId);
    List<String> getFollowers(String followedId, int page, int size) throws ExecutionException, InterruptedException;
    List<String> getFollowing(String followerId, int page, int size) throws ExecutionException, InterruptedException;
    List<String> getFriends(String uid, int page, int size) throws ExecutionException, InterruptedException;
    boolean isFollowing(String followerId, String followedId) throws ExecutionException, InterruptedException;
}
