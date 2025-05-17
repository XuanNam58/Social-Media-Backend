package com.social_media_friend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UserFollowResponse {
    String uid;
    String username;
    String fullName;
    String profilePicURL;
}
