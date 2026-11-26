package org.sopt.haphap.domain.mypage.dto.response;

import java.util.List;

public record MyRegistrationListResponse(
        Counts counts,
        List<MyRegistrationResponse> registrations
) {
    // 탭 숫자. 선택한 필터와 무관하게 항상 전체 기준
    public record Counts(long totalCount, long inProgressCount, long allClosedCount) {}
}
