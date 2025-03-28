package com.example.social_media.service.impl;

import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Firestore firestore;
    UserRepository userRepository;

    @Override
    public User getUserInfo(String token) throws FirebaseAuthException {

        return null;
    }

    @Override
    public boolean isUsernameExists(String username) throws ExecutionException, InterruptedException {
//        firestore.collection("users") truy vấn đến collection có tên "users" trong Firestore.
        CollectionReference users = firestore.collection("users");

        QuerySnapshot querySnapshot = users.whereEqualTo("username", username)
                .limit(1) // giới hạn kết quả trả về để tối ưu hóa hiệu suất
                .get().get();

//        .get() trong Firestore là một hàm bất đồng bộ (asynchronous),
//          tức là nó không trả về ngay lập tức mà cần thời gian để truy vấn dữ liệu từ Firestore.
//         Do Spring Boot mặc định chạy đồng bộ, nên .get().get() được sử dụng để chờ kết quả trước khi tiếp tục xử lý.
        return !querySnapshot.isEmpty();
    }

    @Override
    public void save(User user, String uid) throws ExecutionException, InterruptedException {
        userRepository.save(user, uid);
    }
}
