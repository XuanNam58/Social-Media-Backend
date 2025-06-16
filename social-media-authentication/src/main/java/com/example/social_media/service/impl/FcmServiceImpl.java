package com.example.social_media.service.impl;

import com.example.social_media.service.FcmService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendNotification(String token, String title, String body) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Sent message: " + response);
    }


    @Override
    public void saveTokenToFirebase(String username, String token) {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);
        db.collection("user_tokens").document(username).set(data);
    }

    @Override
    public String getTokenForUser(String username) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("user_tokens").document(username).get().get();
        return doc.contains("fcmToken") ? doc.getString("fcmToken") : null;
    }

    @Override
    public void deleteTokenForUser(String username) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("user_tokens").document(username).delete();
    }

}