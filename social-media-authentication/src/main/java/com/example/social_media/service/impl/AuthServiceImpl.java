package com.example.social_media.service.impl;

import com.example.social_media.config.FirebaseAuthenticationToken;
import com.example.social_media.dto.request.UserSignupRequest;
import com.example.social_media.dto.response.UserLoginResponse;
import com.example.social_media.dto.response.UserSignupResponse;
import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.AuthService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    UserRepository userRepository;
    Firestore firestore;

    @Override
    public UserSignupResponse signUp(UserSignupRequest userSignupReq) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(userSignupReq.getEmail())
                .setPassword(userSignupReq.getPassword());
        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String uid = userRecord.getUid();

        firestore.collection("users").document(uid).set(User.builder()
                .uid(uid)
                .username(userSignupReq.getUsername())
                .fullName(userSignupReq.getFullName())
                .email(userSignupReq.getEmail())
                .createdAt(Timestamp.now())
                .postNum(0L)
                .followerNum(0L)
                .followingNum(0L)
                .build()
        );

        return UserSignupResponse.builder()
                .fullName(userSignupReq.getFullName())
                .username(userSignupReq.getUsername())
                .email(userSignupReq.getEmail())
                .build();
    }

    @Override
    public UserLoginResponse login(String token) throws FirebaseAuthException {
        String idToken = token.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

        FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(Collections.emptyList(), decodedToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return UserLoginResponse.builder()
                .token(idToken)
                .build();
    }
}
