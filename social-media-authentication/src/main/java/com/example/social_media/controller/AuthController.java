package com.example.social_media.controller;

import com.example.social_media.dto.request.UserLoginReq;
import com.example.social_media.dto.request.UserSignupReq;
import com.example.social_media.dto.response.UserLoginRes;
import com.example.social_media.dto.response.UserSignupRes;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignupReq userSignupReq) {
        try {
            UserSignupRes response = authService.signUp(userSignupReq);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error during signup"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginRes> login(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        return new ResponseEntity<>(authService.login(token), HttpStatus.CREATED);
    }


    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.isUsernameExists(username);
            if (exists) {
                return ResponseEntity.ok()
                        .body(Map.of("message", "Username existed", "exists", true));
            } else {
                return ResponseEntity.ok()
                        .body(Map.of("message", "Username available", "exists", false));
            }
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error checking username"));
        }
    }

}
