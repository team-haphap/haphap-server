package org.sopt.haphap.domain.posting.dto;

import java.time.LocalDateTime;

public interface PostingActivityProjection {
    Long getPostingId();
    LocalDateTime getActivityAt();
}