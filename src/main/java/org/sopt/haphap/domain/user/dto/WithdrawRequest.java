package org.sopt.haphap.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sopt.haphap.domain.user.entity.WithdrawalReason;

public record WithdrawRequest(
        @Schema(description = "탈퇴 사유", example = "LOW_USAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawalReason reason,

        @Schema(description = "기타 사유 (reason이 ETC일 때만 필수, 공백 제외 1~200자)", example = "알림이 너무 많이 와요")
        @Size(max = 200, message = "기타 사유는 200자 이하로 입력해주세요.")
        String etcReason
) {
    @AssertTrue(message = "기타 사유를 입력해주세요.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isEtcReasonValid() {
        if (reason != WithdrawalReason.ETC) return true;
        return etcReason != null && !etcReason.isBlank();
    }

    //ETC가 아니면 null, ETC면 앞뒤 공백 제거
    public String normalizedEtcReason() {
        return reason == WithdrawalReason.ETC ? etcReason.strip() : null;
    }
}
