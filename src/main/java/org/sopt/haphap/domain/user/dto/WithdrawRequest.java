package org.sopt.haphap.domain.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sopt.haphap.domain.user.entity.WithdrawalReason;

public record WithdrawRequest (
    @NotNull(message = "탈퇴 사유는 필수입니다.")
    WithdrawalReason reason,

    @Size(max=200, message = "기타 사유는 200자 이하로 입력해주세요.")
    String etcReason
)
    {
        @AssertTrue(message = "기타 사유를 입력해주세요.")
                public boolean isEtcReasonValid() {
        if (reason != WithdrawalReason.ETC) return true;
        return etcReason != null && !etcReason.isBlank();
    }

    //ETC가 아니면 null, ETC면 앞뒤 공백 제거
    public String normalizedEtcReason(){
            return reason == WithdrawalReason.ETC ? etcReason.strip() : null;
    }
}
