package org.sopt.haphap.domain.mypage.dto.response;

public record MyRegistrationResponse(
        Long postingId,
        String companyName,
        String title,
        String logoImageUrl,
        String currentState,   // 공고 상세와 같은 현재 전형. 예: "1차 면접" / "마감" / "진행 예정"
        String myResult        // 전형 순서상 가장 뒤에 등록한 결과. 예: "서류 합격"
) {}
