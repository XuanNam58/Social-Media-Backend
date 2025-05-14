package com.example.social_media.service.impl;

import com.example.social_media.dto.response.UserSearchResponse;
import com.example.social_media.entity.SearchHistory;
import com.example.social_media.service.SearchService;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {
    Firestore firestore;

    @Override
    public List<UserSearchResponse> searchUsers(String query) throws ExecutionException, InterruptedException {
        String lowercaseQuery = query.toLowerCase();

        try {
            // Truy vấn collection users, giới hạn số document đọc để tối ưu
            ApiFuture<QuerySnapshot> future = firestore
                    .collection("users")
                    .limit(100) // Giới hạn để giảm chi phí, điều chỉnh theo nhu cầu
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<UserSearchResponse> results = documents.stream()
                    .map(doc -> {
                        // Ánh xạ sang UserSearchResponse
                        UserSearchResponse response = new UserSearchResponse();
                        response.setUid(doc.getString("uid"));
                        response.setUsername(doc.getString("username"));
                        response.setFullName(doc.getString("fullName"));
                        response.setProfilePicURL(doc.getString("profilePicURL"));
                        return response;
                    })
                    .filter(response -> {
                        // Lọc dựa trên username và fullName
                        String username = response.getUsername();
                        String fullName = response.getFullName();
                        return (username != null && username.toLowerCase().contains(lowercaseQuery)) ||
                                (fullName != null && fullName.toLowerCase().contains(lowercaseQuery));
                    })
                    .limit(10) // Giới hạn tối đa 10 kết quả
                    .collect(Collectors.toList());

            return results;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserSearchResponse> getRecentSearch(String searcherId) {
        try {
            Query query = firestore
                    .collection("search_history")
                    .document(searcherId)
                    .collection("history")
                    .orderBy("searchedAt", Query.Direction.DESCENDING)
                    .limit(5);
            List<SearchHistory> histories = query.get().get()
                    .toObjects(SearchHistory.class);

            List<UserSearchResponse> searchResponses = new ArrayList<>();
            for (SearchHistory history : histories) {
                DocumentSnapshot userDoc = firestore
                        .collection("users")
                        .document(history.getTargetUserId())
                        .get().get();

                if (userDoc.exists()) {
                    UserSearchResponse searchResponse = UserSearchResponse.builder()
                            .uid(userDoc.getString("uid"))
                            .username(userDoc.getString("username"))
                            .fullName(userDoc.getString("fullName"))
                            .profilePicURL(userDoc.getString("profilePicURL"))
                            .build();

                    searchResponses.add(searchResponse);
                }
            }
            return searchResponses;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get search history: " + e.getMessage());
        }
    }

    @Override
    public void addSearchHistory(String searcherId, String targetUserId) {
        try {
            CollectionReference historyRef = firestore
                    .collection("search_history")
                    .document(searcherId)
                    .collection("history");
            QuerySnapshot snapshot = historyRef.get().get();
            if (snapshot.size() >= 10) {
                Query oldestQuery = historyRef.orderBy("searchedAt", Query.Direction.ASCENDING).limit(1);
                oldestQuery.get().get().getDocuments().forEach(doc -> doc.getReference().delete());
            }

            SearchHistory searchHistory = SearchHistory.builder()
                    .searcherId(searcherId)
                    .targetUserId(targetUserId)
                    .searchedAt(Timestamp.now())
                    .build();

            DocumentReference docRef = firestore
                    .collection("search_history")
                    .document(searcherId)
                    .collection("history")
                    .document(targetUserId);

//            searchHistory.setId(docRef.getId());
            docRef.set(searchHistory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add search history: " + e.getMessage());
        }

    }

    @Override
    public void deleteSearchHistory(String searcherId, String targetUserId) {
        try {
            DocumentReference docRef = firestore
                    .collection("search_history")
                    .document(searcherId)
                    .collection("history")
                    .document(targetUserId);
            docRef.delete();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete search history: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllSearchHistory(String searcherId) {
        try {
            // Truy vấn tất cả document trong sub-collection history
            CollectionReference historyRef = firestore
                    .collection("search_history")
                    .document(searcherId)
                    .collection("history");

            // Lấy tất cả document
            ApiFuture<QuerySnapshot> querySnapshot = historyRef.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            WriteBatch batch = firestore.batch();
            for (QueryDocumentSnapshot document : documents) {
                 batch.delete(document.getReference());
            }

            // Thực thi batch
            batch.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete all search history: " + e.getMessage());
        }
    }
}
