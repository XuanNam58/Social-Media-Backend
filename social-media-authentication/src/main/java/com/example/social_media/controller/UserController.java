package com.example.social_media.controller;

import com.example.social_media.dto.request.UpdateFollowRequest;
import com.example.social_media.dto.response.UserFollowRes;
import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.entity.User;
import com.example.social_media.service.UserService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    Firestore firestore;
    UserService userService;

    @GetMapping("/req")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) throws FirebaseAuthException, ExecutionException, InterruptedException {
        String idToken = token.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();

        DocumentSnapshot document = firestore.collection("users").document(uid).get().get();

        if (document.exists()) {
            return ResponseEntity.ok(document.getData());
        } else {
            return ResponseEntity.status(404).body("User not found in Firestore");
        }

    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(userId).get().get();

        if (!document.exists()) {
            return ResponseEntity.status(404).body("User not found in Firestore");
        }

        // Get the entire data map first
        Map<String, Object> data = document.getData();

        // Safely cast the lists
        List<String> followersList = data.containsKey("followers") ?
                (List<String>) data.get("followers") : new ArrayList<>();
        List<String> followingList = data.containsKey("following") ?
                (List<String>) data.get("following") : new ArrayList<>();

        User respone = User.builder()
                .username(document.getString("username"))
                .email(document.getString("email"))
                .fullName(document.getString("fullName"))
                .profilePicURL(document.getString("profilePicURL"))
                .bio(document.getString("bio"))
                .followers(followersList != null ? new HashSet<>(followersList) : new HashSet<>())
                .following(followingList != null ? new HashSet<>(followingList) : new HashSet<>())
                .build();

        Timestamp timestamp = document.getTimestamp("createdAt");
        if (timestamp != null) {
            respone.setCreatedAt(Instant.parse(timestamp.toString()).toString());
        }
        if (document.exists()) {
            System.out.println(document.getData());
            return ResponseEntity.ok(respone);
        }
        else
            return ResponseEntity.status(404).body("User not found in Firestore");
    }

    @PostMapping("/save/{uid}")
    public ResponseEntity<?> saveUser(@RequestBody User user, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.save(user, uid);
        return ResponseEntity.ok().body("Save successfully");
    }


    @PutMapping("/update-follower/{uid}")
    public ResponseEntity<?> updateUserFollower(@RequestBody UpdateFollowRequest request, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.updateUserFollowers(uid, new ArrayList<>(request.getIds()));
        return ResponseEntity.ok().body("Update successfully");
    }

    @PutMapping("/update-following/{uid}")
    public ResponseEntity<?> updateUserFollowing(@RequestBody UpdateFollowRequest request, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.updateUserFollowing(uid, new ArrayList<>(request.getIds()));
        return ResponseEntity.ok().body("Update successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String query) {
        try {
            List<Map<String, Object>> results = userService.searchUsers(query);
            return ResponseEntity.ok(results);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Error search users: " + e.getMessage());
        }
    }

    @GetMapping("/get-user-by-username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Map<String, Object> user = userService.getUserByUsername(username);
            if (user == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(user);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Error getting user: " + e.getMessage());
        }
    }

    @GetMapping("/uid")
    public ResponseEntity<String> getUidByUsername(@RequestParam String username) {
        try {
            String uid = userService.getUidByUsername(username);
            return uid != null
                    ? ResponseEntity.ok(uid)
                    : ResponseEntity.notFound().build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Firestore error");
        }

    }

    @GetMapping("/follower-list")
    public ResponseEntity<List<UserFollowRes>> getFollowers(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        if (idList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getUsersByIds(idList));
    }

    @GetMapping("/following-list")
    public ResponseEntity<List<UserFollowRes>> getFollowings(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        if (idList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getUsersByIds(idList));
    }

    @GetMapping("/friend-list")
    public ResponseEntity<List<UserFollowRes>> getFriends(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<String> idList = Arrays.asList(ids.split(","));
        if (idList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.getUsersByIds(idList));
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
