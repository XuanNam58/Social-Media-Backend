package com.example.social_media.controller;

import com.example.social_media.dto.request.UpdateFollowCountsRequest;
import com.example.social_media.dto.response.ApiResponse;
import com.example.social_media.dto.response.UserFollowResponse;
import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.entity.User;
import com.example.social_media.service.UserService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    Firestore firestore;
    UserService userService;

    @GetMapping("/req")
    public ResponseEntity<ApiResponse<User>> getUserInfo(@RequestHeader("Authorization") String token) throws FirebaseAuthException, ExecutionException, InterruptedException {
        String idToken = token.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();

        DocumentSnapshot document = firestore.collection("users").document(uid).get().get();

        return ResponseEntity.ok()
                .body(ApiResponse.<User>builder()
                        .code(1000)
                        .message("User retrieved successfully")
                        .result(document.toObject(User.class))
                        .build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(userId).get().get();

        // Get the entire data map first
//        Map<String, Object> data = document.getData();

//        User respone = User.builder()
//                .username(document.getString("username"))
//                .email(document.getString("email"))
//                .fullName(document.getString("fullName"))
//                .profilePicURL(document.getString("profilePicURL"))
//                .bio(document.getString("bio"))
//                .createdAt(document.getTimestamp("createdAt"))
//                .build();
        return ResponseEntity.ok()
                .body(ApiResponse.<User>builder()
                        .code(1000)
                        .message("User retrieved successfully")
                        .result(document.toObject(User.class))
                        .build());

    }

    @PostMapping("/save/{uid}")
    public ResponseEntity<?> saveUser(@RequestBody User user, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.save(user, uid);
        return ResponseEntity.ok().body("Save successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchUsers(@RequestParam("q") String query) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> results = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(1000)
                .message("Users retrieved successfully")
                .result(results)
                .build());
    }

    @GetMapping("/get-user-by-username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserByUsername(@PathVariable String username) throws ExecutionException, InterruptedException {
        Map<String, Object> user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .code(1404)
                            .message("User not found")
                            .build());
        }
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("User retrieved successfully")
                .result(user)
                .build());
    }

    @GetMapping("/uid")
    public ResponseEntity<ApiResponse<String>> getUidByUsername(@RequestParam String username) throws ExecutionException, InterruptedException {
        String uid = userService.getUidByUsername(username);
        if (uid == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<String>builder()
                            .code(1404)
                            .message("User not found")
                            .build());
        }
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("UID retrieved successfully")
                .result(uid)
                .build());
    }

    @GetMapping("/follower-list")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowers(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                    .code(1000)
                    .message("No followers retrieved")
                    .result(List.of())
                    .build());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        List<UserFollowResponse> followers = userService.getUsersByIds(idList);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Followers retrieved successfully")
                .result(followers)
                .build());
    }

    @GetMapping("/following-list")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowings(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                    .code(1000)
                    .message("No followings retrieved")
                    .result(List.of())
                    .build());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        List<UserFollowResponse> followings = userService.getUsersByIds(idList);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Followings retrieved successfully")
                .result(followings)
                .build());
    }

    @GetMapping("/friend-list")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFriends(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                    .code(1000)
                    .message("No friends retrieved")
                    .result(List.of())
                    .build());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        List<UserFollowResponse> friends = userService.getUsersByIds(idList);
        return ResponseEntity.ok(ApiResponse.<List<UserFollowResponse>>builder()
                .code(1000)
                .message("Friends retrieved successfully")
                .result(friends)
                .build());
    }

    @PostMapping("/update-follow-counts")
    public ResponseEntity<ApiResponse<Void>> incrementFollower(@RequestBody UpdateFollowCountsRequest request) throws ExecutionException, InterruptedException {
        userService.updateFollowCounts(request.getFollowerId(), request.getFollowedId(), request.getOperation());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("Follow counts updated")
                .build());
    }

    @PostMapping("/batch-by-username")
    public ResponseEntity<?> getUsersByUsernames(@RequestBody Set<String> usernames) {
        try {
            List<UserDTO> users = userService.getUsersByUsernames(usernames);
            return ResponseEntity.ok(users);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Error retrieving users: " + e.getMessage());
        }
    }

}
