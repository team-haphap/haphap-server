package org.sopt.haphap.domain.registration.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegistrationReviewResolveRequest(
        @NotNull(message = "승인 여부는 필수입니다.") Boolean accept
) {}
