package com.social_media_friend.entity;

import com.google.cloud.Timestamp;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UserRelationship {
    String id;
    String followerId;
    String followedId;
    Timestamp followAt;
}
