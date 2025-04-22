package com.example.social_media.controller;

import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.UserService;
import com.google.api.core.ApiFuture;
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

        if (document.exists())
            return ResponseEntity.ok(document.getData());
        else
            return ResponseEntity.status(404).body("User not found in Firestore");
    }

    @PostMapping("/save/{uid}")
    public ResponseEntity<?> saveUser(@RequestBody User user, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.save(user, uid);
        return ResponseEntity.ok().body("Save successfully");
    }


    @PutMapping("/update-follower/{uid}")
    public ResponseEntity<?> updateUserFollower(@RequestBody User user, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.updateUserFollowers(uid, user.getFollowers().stream().toList());
        return ResponseEntity.ok().body("Update successfully");
    }

    @PutMapping("/update-following/{uid}")
    public ResponseEntity<?> updateUserFollowing(@RequestBody User user, @PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.updateUserFollowing(uid, user.getFollowing().stream().toList());
        return ResponseEntity.ok().body("Update successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String query) {
        try {
            List<Map<String, Object>> results = userService.searchUsers(query);
            return ResponseEntity.ok(results);
        } catch (ExecutionException  | InterruptedException e){
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
        } catch (ExecutionException  | InterruptedException e){
            return ResponseEntity.internalServerError().body("Error getting user: " + e.getMessage());
        }
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
