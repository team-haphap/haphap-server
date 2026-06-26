package org.sopt.haphap.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.registration.domain.ContactMethod;
import org.sopt.haphap.registration.domain.RegistrationResult;

import java.time.LocalDate;

public record RegistrationCreateRequest(

        @NotNull(message = "공고 ID는 필수입니다.")
        Long postingId,

        @NotBlank(message = "전형 단계는 필수입니다.")
        @NotNull String stage,

        @NotNull LocalDate contactedAt,
        @NotNull ContactMethod contactMethod,


        @NotNull(message = "전형 결과는 필수입니다.")
        RegistrationResult result,

        boolean anonymous,

        @NotNull(message = "알람 수신 여부는 필수입니다.")
        boolean alarmEnabled
) {
}
