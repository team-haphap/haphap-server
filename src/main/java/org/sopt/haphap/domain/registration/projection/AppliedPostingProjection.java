package org.sopt.haphap.domain.registration.projection;

import java.time.LocalDateTime;

// 유저가 결과를 등록한 공고 + 그 공고에 마지막으로 등록/수정한 시각
public interface AppliedPostingProjection {
    Long getPostingId();
    LocalDateTime getLastActivityAt();
}
