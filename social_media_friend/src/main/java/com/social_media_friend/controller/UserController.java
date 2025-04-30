package com.social_media_friend.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.social_media_friend.dto.response.ApiResponse;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@RequestHeader("Authorization") String token,
                                                    @RequestBody Map<String, String> request)
            throws ExecutionException, InterruptedException {
        userService.followUser(token, request.get("followerId"), request.get("followedId"));
        System.out.println("followerId:" + request.get("followerId"));
        System.out.println("followedId:" + request.get("followedId"));
        return ResponseEntity.ok().body(ApiResponse.<Void>builder()
                .code(1000)
                .message("Follow successfully")
                .build());
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@RequestHeader("Authorization") String token,
                                                      @RequestBody Map<String, String> request)
            throws ExecutionException, InterruptedException {
        userService.unFollowUser(token, request.get("followerId"), request.get("followedId"));
        return ResponseEntity.ok().body(ApiResponse.<Void>builder()
                .code(1000)
                .message("Unfollow successfully")
                .build());
    }

    @GetMapping("/followers/{followedId}")
    public ResponseEntity<ApiResponse<List<String>>> getFollowers(@PathVariable String followedId, @RequestParam int page, @RequestParam int size) throws ExecutionException, InterruptedException {
        List<String> followerIds = userService.getFollowers(followedId, page, size);
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .code(1000)
                .message("Followers retrieved successfully")
                .result(followerIds)
                .build());
    }

    @GetMapping("/following/{followerId}")
    public ResponseEntity<ApiResponse<List<String>>> getFollowing(@PathVariable String followerId, @RequestParam int page, @RequestParam int size) throws ExecutionException, InterruptedException {
        List<String> followedIds = userService.getFollowing(followerId, page, size);
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .code(1000)
                .message("Following retrieved successfully")
                .result(followedIds)
                .build());
    }

    @GetMapping("/friends/{uid}")
    public ResponseEntity<ApiResponse<List<String>>> getFriends(@PathVariable String uid, @RequestParam int page, @RequestParam int size, @RequestHeader("Authorization") String authHeader) throws ExecutionException, InterruptedException, FirebaseAuthException {
        List<String> friendUids = userService.getFriends(uid, page, size);
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .code(1000)
                .message("Friends retrieved successfully")
                .result(friendUids)
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
