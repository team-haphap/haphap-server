package org.sopt.haphap.domain.mypage.dto.request;

// [내 지원 내역] 전체보기 탭 필터. 마감 여부는 공고 상세의 [마감] 판정(Posting.isClosed)과 같다.
public enum MyRegistrationFilter {
    ALL,
    IN_PROGRESS,
    ALL_CLOSED;

    public boolean matches(boolean closed) {
        return switch (this) {
            case ALL -> true;
            case IN_PROGRESS -> !closed;
            case ALL_CLOSED -> closed;
        };
    }
}
