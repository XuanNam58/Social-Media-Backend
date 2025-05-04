package com.example.social_media.service;

import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.dto.response.UserFollowResponse;
import com.example.social_media.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface UserService {

    boolean isUsernameExists(String username) throws ExecutionException, InterruptedException;
    void save(User user, String uid) throws ExecutionException, InterruptedException;

    List<Map<String, Object>> searchUsers(String query) throws ExecutionException, InterruptedException;
    Map<String, Object> getUserByUsername(String username) throws ExecutionException, InterruptedException;
    List<UserDTO> getUsersByUsernames(Set<String> usernames) throws ExecutionException, InterruptedException;
    String getUidByUsername(String username) throws ExecutionException, InterruptedException;
    List<UserFollowResponse> getUsersByIds(List<String> ids);
    void updateFollowCounts(String followerId, String followedId, String operation) throws ExecutionException, InterruptedException;
}
