package org.sopt.haphap.domain.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.domain.registration.domain.ContactMethod;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

import java.time.LocalDate;

public record RegistrationCreateRequest(

        @NotNull(message = "공고 ID는 필수입니다.")
        Long postingId,

        @NotBlank(message = "전형 단계는 필수입니다.")
        String stage,

        @NotNull(message = "연락 날짜는 필수입니다.")
        LocalDate contactedAt,

        @NotNull(message = "연락 수단은 필수입니다.")
        ContactMethod contactMethod,

        @NotNull(message = "전형 결과는 필수입니다.")
        RegistrationResult result,

        @NotNull(message = "익명 여부는 필수입니다.")
        boolean anonymous,

        @NotNull(message = "알람 수신 여부는 필수입니다.")
        boolean alarmEnabled,

        boolean force
) {
}
