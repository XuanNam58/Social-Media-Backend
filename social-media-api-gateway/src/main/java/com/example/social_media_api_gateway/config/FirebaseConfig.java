package com.example.social_media_api_gateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;
    @PostConstruct
    public void initializeFirebase() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream(serviceAccountPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}
