package com.example.social_media.service;

import com.example.social_media.dto.request.UserLoginReq;
import com.example.social_media.dto.request.UserSignupReq;
import com.example.social_media.dto.response.UserLoginRes;
import com.example.social_media.dto.response.UserSignupRes;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.concurrent.ExecutionException;

public interface AuthService {
    UserSignupRes signUp(UserSignupReq userSignupReq) throws FirebaseAuthException, ExecutionException, InterruptedException;
    UserLoginRes login(String token) throws FirebaseAuthException;
}
