package com.example.social_media.service.impl;

import com.example.social_media.config.FirebaseAuthenticationToken;
import com.example.social_media.dto.request.UserLoginReq;
import com.example.social_media.dto.request.UserSignupReq;
import com.example.social_media.dto.response.UserLoginRes;
import com.example.social_media.dto.response.UserSignupRes;
import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.AuthService;
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

    @Override
    public UserSignupRes signUp(UserSignupReq userSignupReq) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(userSignupReq.getEmail())
                .setPassword(userSignupReq.getPassword());
        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String uid = userRecord.getUid();

        userRepository.save(User.builder()
                .fullName(userSignupReq.getFullName())
                .username(userSignupReq.getUsername())
                .email(userSignupReq.getEmail())
                .build(), uid);

        return UserSignupRes.builder()
                .fullName(userSignupReq.getFullName())
                .username(userSignupReq.getUsername())
                .email(userSignupReq.getEmail())
                .build();
    }

    @Override
    public UserLoginRes login(String token) throws FirebaseAuthException {
        String idToken = token.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

        FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(Collections.emptyList(), decodedToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return UserLoginRes.builder()
                .email(decodedToken.getEmail())
                .build();
    }
}
