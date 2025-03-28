package com.social_media_friend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.social_media_friend.dto.response.UserResponse;
import com.social_media_friend.entity.User;
import com.social_media_friend.repository.UserRepository;
import com.social_media_friend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Firestore firestore;
    UserRepository userRepository;
    RestTemplate restTemplate;
    String AUTH_SERVICE_URL = "http://localhost:8080/api/users/";

    @Override
    public void followUser(String token, String reqUserId, String followUserId) throws ExecutionException, InterruptedException {
        try {
            // 1. Lấy thông tin user
            UserResponse reqUser = getUserById(token, reqUserId);
            UserResponse followUser = getUserById(token, followUserId);

            // 2. Kiểm tra trùng lặp
            if (reqUser.getFollowing().contains(followUserId)) {
                throw new RuntimeException("Already following");
            }

            // 3. Cập nhật danh sách
            reqUser.getFollowing().add(followUserId);
            followUser.getFollowers().add(reqUserId);

            // 4. Gọi API Auth Service
            updateAuthServiceUser(token, reqUser, reqUserId);
            updateAuthServiceUser(token, followUser, followUserId);

            // 5. Lưu vào Friend Service
            userRepository.save(
                    User.builder().following(reqUser.getFollowing()).build(),
                    reqUserId
            );
            userRepository.save(
                    User.builder().followers(followUser.getFollowers()).build(),
                    followUserId
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service", e);
        }
    }

    private void updateAuthServiceUser(String token, UserResponse user, String userId) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<UserResponse> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                AUTH_SERVICE_URL + "save/" + userId,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update user in auth service");
        }
    }

    @Override
    public UserResponse getUserById(String token, String userId) {
        HttpHeaders headers = createHeaders(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(AUTH_SERVICE_URL + userId, HttpMethod.GET, entity, UserResponse.class);
        return (UserResponse) response.getBody();
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
