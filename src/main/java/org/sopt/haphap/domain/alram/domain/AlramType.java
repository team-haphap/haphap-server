package org.sopt.haphap.domain.alram.domain;

public enum AlramType {
    NEW_STAGE_REGISTRATION,
    STAGE_PASSED,        // (공고x전형) 최초 합격 인증 승인
    POSTING_CLOSED,      // 공고 최초 마감
    DEADLINE,            // 마감 임박 (확장용)
    ANNOUNCEMENT         // 발표 예정 (확장용)
}
