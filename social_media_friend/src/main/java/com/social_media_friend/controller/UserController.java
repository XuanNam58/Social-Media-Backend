package com.social_media_friend.controller;


import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestHeader("Authorization") String token,
                                    @RequestBody Map<String, String> request)
            throws ExecutionException, InterruptedException {
        userService.followUser(token, request.get("followId"), request.get("followedId"));
        return ResponseEntity.ok().body("Update successfully");
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestHeader("Authorization") String token,
                                      @RequestBody Map<String, String> request)
            throws ExecutionException, InterruptedException {
        userService.unFollowUser(token, request.get("followerId"), request.get("followedId"));
        return ResponseEntity.ok().body("Update successfully");
    }

}
