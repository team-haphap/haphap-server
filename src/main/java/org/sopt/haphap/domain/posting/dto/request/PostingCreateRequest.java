package org.sopt.haphap.domain.posting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PostingCreateRequest(
        @NotBlank(message = "공고명은 필수입니다.") String title,
        LocalDate deadline,
        String location,
        String position,
        @NotNull(message = "카테고리 ID는 필수입니다.") Long categoryId,
        @NotNull(message = "회사 ID는 필수입니다.") Long companyId
) {}