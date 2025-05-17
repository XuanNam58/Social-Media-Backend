package com.example.social_media.controller;

import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.dto.request.UpdateFollowCountsRequest;
import com.example.social_media.dto.response.ApiResponse;
import com.example.social_media.dto.response.UserFollowResponse;
import com.example.social_media.dto.response.UserSearchResponse;
import com.example.social_media.entity.User;
import com.example.social_media.service.SearchService;
import com.example.social_media.service.UserService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.ws.rs.Path;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    Firestore firestore;
    SearchService searchService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(@RequestParam("q") String query) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok()
                .body(ApiResponse.<List<UserSearchResponse>>builder()
                        .code(1000)
                        .message("User retrieved successfully")
                        .result(searchService.searchUsers(query))
                        .build());
    }

    @GetMapping("/{searcherId}")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> getRecentSearch(@PathVariable String searcherId) {
        return ResponseEntity.ok()
                .body(ApiResponse.<List<UserSearchResponse>>builder()
                        .code(1000)
                        .message("User retrieved successfully")
                        .result(searchService.getRecentSearch(searcherId))
                        .build());
    }

    @PostMapping("/add-search-history")
    public ResponseEntity<ApiResponse<Void>> addSearchHistory(@RequestParam("searcherId") String searcherId,
                                                              @RequestParam("targetUserId") String targetUserId) {
        searchService.addSearchHistory(searcherId, targetUserId);
        return ResponseEntity.ok()
                .body(ApiResponse.<Void>builder()
                        .code(1000)
                        .message("Add search history successfully")
                        .build());
    }

    @DeleteMapping("/delete-search-history")
    public ResponseEntity<ApiResponse<Void>> deleteSearchHistory(@RequestParam("searcherId") String searcherId,
                                                                 @RequestParam("targetUserId") String targetUserId) {
        searchService.deleteSearchHistory(searcherId, targetUserId);
        return ResponseEntity.ok()
                .body(ApiResponse.<Void>builder()
                        .code(1000)
                        .message("Delete search history successfully")
                        .build());
    }

    @DeleteMapping("/delete-all-search-history")
    public ResponseEntity<ApiResponse<Void>> deleteAllSearchHistory(@RequestParam("searcherId") String searcherId) {
        searchService.deleteAllSearchHistory(searcherId);
        return ResponseEntity.ok()
                .body(ApiResponse.<Void>builder()
                        .code(1000)
                        .message("Delete all search history successfully")
                        .build());
    }
}
