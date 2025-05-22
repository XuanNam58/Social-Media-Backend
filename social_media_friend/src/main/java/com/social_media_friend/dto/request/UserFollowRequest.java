package com.social_media_friend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UserFollowRequest {
    String followerFullname;
    String followerUsername;
    String followedUsername;
    String followerId;
    String followedId;
}
