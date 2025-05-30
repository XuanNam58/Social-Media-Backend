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
public class SearchHistory {
    String id;
    String searcherId;
    String targetUserId;
    Timestamp searchedAt;
}
