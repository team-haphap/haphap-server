package org.sopt.haphap.domain.registration.dto.request;

import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.domain.registration.domain.ContactMethod;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistrationCreateRequest(

        @NotNull(message = "공고 ID는 필수입니다.")
        Long postingId,

        @NotNull(message = "전형 단계는 필수입니다.")
        Long stageId,

        LocalDateTime contactedAt,

        ContactMethod contactMethod,

        @NotNull(message = "전형 결과는 필수입니다.")
        RegistrationResult result,

        @NotNull(message = "익명 여부는 필수입니다.")
        Boolean anonymous,

        @NotNull(message = "알람 수신 여부는 필수입니다.")
        Boolean alarmEnabled
) {
}
