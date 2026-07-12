package org.sopt.haphap.domain.registration.projection;

import java.time.LocalDateTime;

public interface RecentParticipantProjection {
    Long getUserId();
    String getProfileImageUrl();   // 이미지 URL (User에 추가할 필드)
    LocalDateTime getLastActivity();
}