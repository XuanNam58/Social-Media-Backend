package com.example.social_media.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class UpdateFollowCountsRequest {
    String followerId;
    String followedId;
    String operation;
}
