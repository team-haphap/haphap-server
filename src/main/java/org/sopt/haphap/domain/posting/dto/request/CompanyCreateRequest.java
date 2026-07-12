package org.sopt.haphap.domain.posting.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequest(
        @NotBlank(message = "회사명은 필수입니다.") String name,
        String logoImageUrl,
        String imageUrl,
        String cardLogoImageUrl
) {}
