package com.example.social_media.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
    LocalDate createdAt;
    Set<String> followers;
    Set<String> following;
    List<String> posts;

}
