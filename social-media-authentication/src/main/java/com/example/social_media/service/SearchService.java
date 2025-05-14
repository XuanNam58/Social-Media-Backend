package com.example.social_media.service.impl;

import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.dto.response.UserSearchResponse;

import java.util.List;

public interface SearchService {
    List<UserSearchResponse> getRecentSearch(String searcherId);
    void addSearchHistory(String searcherId, String targetUserId);
    void deleteSearchHistory(String searcherId,String targetUserId);
    void deleteAllSearchHistory(String searcherId);
}
