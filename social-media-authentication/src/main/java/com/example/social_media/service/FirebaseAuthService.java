package com.example.social_media.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public interface FirebaseAuthService {
    FirebaseToken verifyToken(String idToken)  throws FirebaseAuthException;
}
