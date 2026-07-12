package org.sopt.haphap.domain.posting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PostingStageCreateRequest(
        @NotBlank(message = "전형명은 필수입니다.") String name,
        @NotNull(message = "전형 순서는 필수입니다.") Integer orderIndex,
        LocalDate expectedAnnouncementDate,
        @NotNull(message = "합격 예상 점수는 필수입니다.") Integer expectedScore
) {}