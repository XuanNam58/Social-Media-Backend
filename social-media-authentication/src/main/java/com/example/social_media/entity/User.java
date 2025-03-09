package com.example.social_media.entity;

import com.google.cloud.Timestamp;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    Timestamp createdAt;
    Set<String> followers = new HashSet<>();
    Set<String> following = new HashSet<>();
    List<String> posts = new ArrayList<>();

}
