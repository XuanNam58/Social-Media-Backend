package com.example.social_media.service.impl;

import com.cloudinary.Cloudinary;
import com.example.social_media.dto.information.UserDTO;
import com.example.social_media.dto.request.UpdateFollowCountsRequest;
import com.example.social_media.dto.response.UserFollowResponse;
import com.example.social_media.entity.User;
import com.example.social_media.repository.UserRepository;
import com.example.social_media.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    Cloudinary cloudinary;
    Firestore firestore;
    UserRepository userRepository;
    RedisTemplate<String, Long> redisTemplate;
    KafkaTemplate<String, UpdateFollowCountsRequest> kafkaTemplate;

    //    MeterRegistry meterRegistry;
    static final ExecutorService executor = Executors.newFixedThreadPool(10);

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
    public List<UserFollowResponse> getUsersByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<UserFollowResponse> results = new ArrayList<>();
        List<String> missingIds = new ArrayList<>();

        try {
            for (String id : ids) {
                Map<Object, Object> userData = redisTemplate.opsForHash().entries("user:" + id);
                if (!userData.isEmpty()) {
                    results.add(UserFollowResponse.builder()
                            .uid(id)
                            .username((String) userData.get("username"))
                            .fullName((String) userData.get("fullName"))
                            .profilePicURL((String) userData.get("profilePicURL"))
                            .build());
                } else {
                    missingIds.add(id);
                }
            }

            if (!missingIds.isEmpty()) {
                List<DocumentReference> refs = missingIds.stream()
                        .map(id -> firestore.collection("users").document(id))
                        .collect(Collectors.toList());
                ApiFuture<List<DocumentSnapshot>> future = firestore.getAll(refs.toArray(new DocumentReference[0]));
                List<DocumentSnapshot> documents = future.get();
                for (DocumentSnapshot doc : documents) {
                    if (doc.exists()) {
                        UserFollowResponse response = UserFollowResponse.builder()
                                .uid(doc.getId())
                                .username(doc.getString("username"))
                                .fullName(doc.getString("fullName"))
                                .profilePicURL(doc.getString("profilePicURL"))
                                .build();
                        results.add(response);
                        Map<String, String> userData = new HashMap<>();
                        userData.put("username", response.getUsername());
                        userData.put("fullName", response.getFullName());
                        userData.put("profilePicURL", response.getProfilePicURL());
                        redisTemplate.opsForHash().putAll("user:" + doc.getId(), userData);
                        redisTemplate.expire("user:" + doc.getId(), 15, TimeUnit.MINUTES);
                    }
                }
            }
        } catch (Exception e) {
            return getUsersByIdsFallback(ids);
        }

        return results;
    }

    @Override
    public List<String> getUsernamesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            // Tạo danh sách DocumentReference
            List<DocumentReference> docRefs = ids.stream()
                    .map(id -> firestore.collection("users").document(id))
                    .collect(Collectors.toList());

            // Lấy tất cả documents cùng lúc
            List<DocumentSnapshot> documents = firestore.getAll(docRefs.toArray(new DocumentReference[0])).get();

            return documents.stream()
                    .filter(DocumentSnapshot::exists)
                    .map(doc -> doc.getString("username"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching documents: " + e.getMessage());
            return List.of();
        }
    }

    private List<UserFollowResponse> getUsersByIdsFallback(List<String> ids) {
        // Logic hiện tại của getUsersByIds
        return ids.stream()
                .map(id -> {
                    try {
                        DocumentSnapshot document = firestore.collection("users").document(id).get().get();
                        if (!document.exists()) {
                            return null;
                        }
                        return UserFollowResponse.builder()
                                .uid(id)
                                .username(document.getString("username"))
                                .fullName(document.getString("fullName"))
                                .profilePicURL(document.getString("profilePicURL"))
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void updateFollowCounts(String followerId, String followedId, String operation) throws ExecutionException, InterruptedException {
        String followingKey = "followingNum:" + followerId;
        String followerKey = "followerNum:" + followedId;
        long ttlHour = 24;

        try {
            Long followingNum = redisTemplate.opsForValue().get(followingKey);
            Long followerNum = redisTemplate.opsForValue().get(followerKey);

            if ("increment".equals(operation)) {
                redisTemplate.opsForValue().increment(followingKey);
                redisTemplate.opsForValue().increment(followerKey);
            } else if ("decrement".equals(operation)) {
                redisTemplate.opsForValue().set(followingKey, Math.max(0L, followingNum != null ? followingNum - 1 : 0));
                redisTemplate.opsForValue().set(followerKey, Math.max(0L, followerNum != null ? followerNum - 1 : 0));
            } else {
                throw new IllegalArgumentException("Invalid operation: " + operation);
            }

            redisTemplate.expire(followingKey, ttlHour, TimeUnit.HOURS);
            redisTemplate.expire(followerKey, ttlHour, TimeUnit.HOURS);

            kafkaTemplate.send("follow-updates", new UpdateFollowCountsRequest(followerId, followedId, operation));
        } catch (Exception e) {
            updateFirestoreDirectly(followerId, followedId, operation);
        }

    }

    @Override
    public void updateUserProfilePic(String uid, MultipartFile file) throws IOException {
        // Tải ảnh lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                Map.of("folder", "profile_pics")); // Lưu ảnh vào thư mục profile_pics

        // Lấy URL
        String imageUrl = uploadResult.get("secure_url").toString();

        // Gọi updateUser để lưu URL vào Firestore
        updateUser(uid, "profilePicURL", imageUrl);
    }


    @Override
    public void updateUser(String uid, String type, String content) {
        // Validate inputs
        if (uid == null || uid.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        DocumentReference userDoc = firestore.collection("users").document(uid);
        switch (type) {
            case "fullName":
                userDoc.update("fullName", content);
                break;
            case "profilePicURL":
                userDoc.update("profilePicURL", content);
                break;
            case "bio":
                userDoc.update("bio", content);
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }


    private void updateFirestoreDirectly(String followerId, String followedId, String operation) throws ExecutionException, InterruptedException {
        DocumentReference followerDoc = firestore.collection("users").document(followerId);
        DocumentReference followedDoc = firestore.collection("users").document(followedId);

        firestore.runTransaction(transaction -> {
            if ("increment".equals(operation)) {
                transaction.update(followerDoc, "followingNum", FieldValue.increment(1));
                transaction.update(followedDoc, "followerNum", FieldValue.increment(1));
            } else if ("decrement".equals(operation)) {
                DocumentSnapshot followerSnap = transaction.get(followerDoc).get();
                DocumentSnapshot followedSnap = transaction.get(followedDoc).get();
                Long followingNum = followerSnap.getLong("followingNum") != null ? followerSnap.getLong("followingNum") : 0L;
                Long followerNum = followedSnap.getLong("followerNum") != null ? followedSnap.getLong("followerNum") : 0L;
                transaction.update(followerDoc, "followingNum", Math.max(0L, followingNum - 1));
                transaction.update(followedDoc, "followerNum", Math.max(0L, followerNum - 1));
            }
            return null;
        }).get();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
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
