package com.social_media_friend.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UserRelationship {
    String followerId;
    String followedId;
    Instant createdAt;
}
