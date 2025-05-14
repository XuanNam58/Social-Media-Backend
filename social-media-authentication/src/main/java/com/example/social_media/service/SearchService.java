package com.example.social_media.service;

import com.example.social_media.dto.response.UserSearchResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SearchService {
    List<UserSearchResponse> searchUsers(String query) throws ExecutionException, InterruptedException;
    List<UserSearchResponse> getRecentSearch(String searcherId);
    void addSearchHistory(String searcherId, String targetUserId);
    void deleteSearchHistory(String searcherId,String targetUserId);
    void deleteAllSearchHistory(String searcherId);
}
