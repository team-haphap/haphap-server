package org.sopt.haphap.domain.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record MyRegistrationPreviewResponse(
        Long postingId,
        String title,
        String currentState,         // 전체보기와 같은 현재 전형. 예: "서류" / "마감" / "진행 예정"
        long applicantCount,         // 공고 상세의 참여 인원과 같은 기준 (상태 등록한 유저 수)
        String logoImageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime appliedAt      // 이 공고에 마지막으로 결과를 등록/수정한 시각
) {}
