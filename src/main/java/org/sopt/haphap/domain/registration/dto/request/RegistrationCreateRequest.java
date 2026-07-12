package org.sopt.haphap.domain.registration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.domain.registration.domain.ContactMethod;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record RegistrationCreateRequest(

        @NotNull(message = "공고 ID는 필수입니다.")
        Long postingId,

        @NotNull(message = "전형 단계는 필수입니다.")
        Long stageId,

        LocalDate contactedDate,   // 날짜 (예: 2026-07-09)
        LocalTime contactedTime,   // 시간 (예: 14:30)

        @Schema(description = "연락받은 채널 목록 (PENDING이면 생략/빈 리스트). 여러 채널 선택 가능",
                example = "[\"EMAIL\", \"SMS\"]",
                allowableValues = {"EMAIL", "SMS", "PHONE", "KAKAO"})
        List<String> contactMethods,

        @NotNull(message = "전형 결과는 필수입니다.")
        RegistrationResult result,

        @NotNull(message = "익명 여부는 필수입니다.")
        Boolean anonymous,

        @NotNull(message = "알람 수신 여부는 필수입니다.")
        Boolean alarmEnabled
) {
        // 서버에서 하나로 합치는 편의 메서드
        public LocalDateTime contactedAt() {
                if (contactedDate == null || contactedTime == null) {
                        return null;
                }
                return LocalDateTime.of(contactedDate, contactedTime);
        }
}
