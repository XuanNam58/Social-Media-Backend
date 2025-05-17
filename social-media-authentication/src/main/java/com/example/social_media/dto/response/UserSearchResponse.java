package com.example.social_media.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UserSearchResponse {
    String uid;
    String username;
    String fullName;
    String profilePicURL;
}
