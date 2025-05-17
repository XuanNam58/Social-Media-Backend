package com.social_media_friend.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.social_media_friend.dto.response.ApiResponse;
import com.social_media_friend.dto.response.UserFollowResponse;
import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/friend/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@RequestBody Map<String, String> request) {
        userService.followUser(request.get("followerId"), request.get("followedId"));
        return ResponseEntity.ok().body(ApiResponse.<Void>builder()
                .code(1000)
                .message("Follow successfully")
                .build());
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @RequestBody Map<String, String> request) {
        userService.unFollowUser(request.get("followerId"), request.get("followedId"));
        return ResponseEntity.ok().body(ApiResponse.<Void>builder()
                .code(1000)
                .message("Unfollow successfully")
                .build());
    }

    @GetMapping("/followers/{followedId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowers(@PathVariable String followedId, @RequestParam int page, @RequestParam int size) throws ExecutionException, InterruptedException {
        List<UserFollowResponse> followers = userService.getFollowers(followedId, page, size);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Followers retrieved successfully")
                .result(followers)
                .build());
    }

    @GetMapping("/following/{followerId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowing(@PathVariable String followerId, @RequestParam int page, @RequestParam int size) throws ExecutionException, InterruptedException {
        List<UserFollowResponse> following = userService.getFollowing(followerId, page, size);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Following retrieved successfully")
                .result(following)
                .build());
    }

    @GetMapping("/friends/{uid}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFriends(@PathVariable String uid, @RequestParam int page, @RequestParam int size, @RequestHeader("Authorization") String authHeader) throws ExecutionException, InterruptedException, FirebaseAuthException {
        List<UserFollowResponse> friends = userService.getFriends(uid, page, size);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Friends retrieved successfully")
                .result(friends)
                .build());
    }

    @GetMapping("/check-following")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(@RequestParam("followerId") String followerId, @RequestParam("followedId") String followedId) throws ExecutionException, InterruptedException {
        boolean isFollowing = userService.isFollowing(followerId, followedId);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(1000)
                .message("Check successfully")
                .result(isFollowing)
                .build());
    }

}
