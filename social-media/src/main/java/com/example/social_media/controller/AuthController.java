package com.example.social_media.controller;

import com.example.social_media.dto.request.UserLoginReq;
import com.example.social_media.dto.request.UserSignupReq;
import com.example.social_media.dto.response.UserLoginRes;
import com.example.social_media.dto.response.UserSignupRes;
import com.example.social_media.service.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<UserSignupRes> signUp(@RequestBody UserSignupReq userSignupReq) throws ExecutionException, FirebaseAuthException, InterruptedException {
        return new ResponseEntity<>(authService.signUp(userSignupReq), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginRes> login(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        return new ResponseEntity<>(authService.login(token), HttpStatus.CREATED);
    }



}
