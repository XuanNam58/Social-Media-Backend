package com.example.social_media.controller;

import com.example.social_media.dto.request.TokenRequest;
import com.example.social_media.dto.request.UserSignupRequest;
import com.example.social_media.dto.response.ApiResponse;
import com.example.social_media.dto.response.UserLoginResponse;
import com.example.social_media.dto.response.UserSignupResponse;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.FcmService;
import com.example.social_media.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;
    UserService userService;
    private final FcmService fcmService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignupResponse>> signUp(@RequestBody UserSignupRequest userSignupReq) throws ExecutionException, FirebaseAuthException, InterruptedException {

        UserSignupResponse response = authService.signUp(userSignupReq);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<UserSignupResponse>builder()
                        .code(1000)
                        .message("Signup successfully")
                        .result(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        return ResponseEntity
                .ok()
                .body(ApiResponse.<UserLoginResponse>
                                builder()
                        .code(1000)
                        .message("Login successfully")
                        .result(authService.login(token))
                        .build());
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) throws ExecutionException, InterruptedException {
        boolean exists = userService.isUsernameExists(username);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(1000)
                .message(exists ? "Username existed" : "Username available")
                .result(exists)
                .build());
    }

    @PostMapping("/fcm/token")
    public ResponseEntity<?> saveToken(@RequestBody TokenRequest request) {
        fcmService.saveTokenToFirebase(request.getUsername(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/{username}")
    public ResponseEntity<String> getTokenForUser(
            @PathVariable String username,
            @RequestBody String content) {
        try {
            String token = fcmService.getTokenForUser(username);
            fcmService.sendNotification(token, "New notification", content);
            if (token != null) {
                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("deleteToken/{username}")
    public ResponseEntity<?> deleteToken(@PathVariable String username) {
        fcmService.deleteTokenForUser(username);
        return ResponseEntity.noContent().build();
    }


}
