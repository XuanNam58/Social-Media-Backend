package com.example.social_media.service.impl;

import com.example.social_media.dto.response.UserFollowRes;
import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Firestore firestore;
    UserRepository userRepository;
    static final ExecutorService executor = Executors.newFixedThreadPool(10);
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

    @Override
    public void updateUserFollowers(String uid, List<String> followers) throws ExecutionException, InterruptedException {
        userRepository.updateUserFollowers(uid, followers);
    }

    @Override
    public void updateUserFollowing(String uid, List<String> following) throws ExecutionException, InterruptedException {
        userRepository.updateUserFollowing(uid, following);
    }

    @Override
    public List<Map<String, Object>> searchUsers(String query) throws ExecutionException, InterruptedException {
        String lowcaseQuery = query.toLowerCase();

        ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents
                .stream()
                .map(doc -> doc.getData())
                .filter(userData -> {
                    String username = (String) userData.get("username");
                    String fullName = (String) userData.get("fullName");
                    return (username != null && username.toLowerCase().contains(lowcaseQuery)) ||
                            (fullName != null && fullName.toLowerCase().contains(lowcaseQuery));
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserByUsername(String username) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("username", username).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty())
            return null;
        return documents.get(0).getData();
    }

    @Override
    public String getUidByUsername(String username) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .get();
        if (!querySnapshot.isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
            return document.getId();
        }
        return null;
    }

    @Override
    public List<UserFollowRes> getUsersByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<UserFollowRes>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        DocumentSnapshot document = firestore.collection("users").document(id)
                                .get()
                                .get();
                        if (!document.exists()) {
                            return null;
                        }
                        return UserFollowRes.builder()
                                .uid(id)
                                .username(document.getString("username"))
                                .fullName(document.getString("fullName"))
                                .profilePicURL(document.getString("profilePicURL"))
                                .build();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    } catch (ExecutionException e) {
                        return null;
                    }
                }, executor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList())
                .join();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

}
