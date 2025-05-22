package com.social_media_friend.service;


import com.social_media_friend.dto.request.UserFollowRequest;
import com.social_media_friend.dto.response.UserFollowResponse;
import com.social_media_friend.dto.response.UserResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {
    void followUser(UserFollowRequest request);

    void unFollowUser(String followerId, String followedId);

    UserResponse getUserById(String token, String userId);
    List<UserFollowResponse> getFollowers(String followedId, int page, int size) throws ExecutionException, InterruptedException;
    List<UserFollowResponse> getFollowing(String followerId, int page, int size) throws ExecutionException, InterruptedException;
    List<UserFollowResponse> getFriends(String uid, int page, int size) throws ExecutionException, InterruptedException;
    boolean isFollowing(String followerId, String followedId) throws ExecutionException, InterruptedException;

    List<String> getFollowersWithUsername(String followedId) throws ExecutionException, InterruptedException;
    List<String> getFollowingWithUsername(String followerId) throws ExecutionException, InterruptedException;
}
