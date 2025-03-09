package com.example.social_media.service;

import com.example.social_media.entity.User;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.concurrent.ExecutionException;

public interface UserService {
    User getUserInfo(String token) throws FirebaseAuthException;
    boolean isUsernameExists(String username) throws ExecutionException, InterruptedException;
}
