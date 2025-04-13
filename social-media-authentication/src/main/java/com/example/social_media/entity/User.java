package com.example.social_media.entity;

import com.google.cloud.Timestamp;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.cloud.firestore.annotation.PropertyName;
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class User {
    String username;
    String email;
    String fullName;
    String profilePicURL;
    String bio;
    String createdAt;
    Set<String> followers = new HashSet<>();
    Set<String> following = new HashSet<>();
    List<String> posts = new ArrayList<>();

}
