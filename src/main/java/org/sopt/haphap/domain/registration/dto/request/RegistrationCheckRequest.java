package org.sopt.haphap.domain.registration.dto.request;

import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

public record RegistrationCheckRequest (
        @NotNull(message = "전형 결과는 필수입니다.")
        RegistrationResult result
){
}