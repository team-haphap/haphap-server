package org.sopt.haphap.domain.posting.domain;

public enum CompanyImageType {
    POPULAR,       // 홈 최근 결과가 올라온 공고 = 검색 인기공고 (같은 거 사용)
    LISTING,       // 공고 리스트 - 검색결과
    DETAIL,        // 상세페이지
    TODAY_LOGO,    // 홈 오늘 발표 예상공고 로고
    CALENDAR_LOGO  // 캘린더 로고
}