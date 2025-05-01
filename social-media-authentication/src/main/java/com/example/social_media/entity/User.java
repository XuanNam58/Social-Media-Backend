package com.example.social_media.entity;

import com.google.cloud.Timestamp;
import lombok.*;
import lombok.experimental.FieldDefaults;
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class User {
    String uid;
    String username;
    String email;
    String fullName;
    String profilePicURL;
    String bio;
    Long postNum;
    Long followerNum;
    Long followingNum;
    Timestamp createdAt;


}
