package com.example.social_media.service;

import com.example.social_media.dto.request.UserSignupRequest;
import com.example.social_media.dto.response.UserLoginResponse;
import com.example.social_media.dto.response.UserSignupResponse;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.concurrent.ExecutionException;

public interface AuthService {
    UserSignupResponse signUp(UserSignupRequest userSignupReq) throws FirebaseAuthException, ExecutionException, InterruptedException;
    UserLoginResponse login(String token) throws FirebaseAuthException;
}
