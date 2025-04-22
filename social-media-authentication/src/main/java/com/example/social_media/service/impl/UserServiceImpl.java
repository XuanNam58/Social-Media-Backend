package com.example.social_media.service.impl;

import com.example.social_media.dto.information.UserDTO;
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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    public List<UserDTO> getUsersByUsernames(Set<String> usernames) throws ExecutionException, InterruptedException {
        List<UserDTO> result = new ArrayList<>();

        CollectionReference usersRef = firestore.collection("users");

        for (String username : usernames) {
            Query query = usersRef.whereEqualTo("username", username);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (!documents.isEmpty()) {
                DocumentSnapshot doc = documents.get(0);
                UserDTO dto = new UserDTO();
                dto.setUsername(username);
                dto.setFullName((String) doc.get("fullName"));
                dto.setProfilePicURL((String) doc.get("profilePicURL"));
                result.add(dto);
            }
        }

        return result;
    }

}
