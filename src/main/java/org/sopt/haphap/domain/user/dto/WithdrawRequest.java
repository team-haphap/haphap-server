package org.sopt.haphap.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sopt.haphap.domain.user.entity.WithdrawalReason;

import java.text.BreakIterator;

public record WithdrawRequest(
        @Schema(description = "탈퇴 사유", example = "LOW_USAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawalReason reason,

        @Schema(description = "기타 사유 (reason이 ETC일 때만 필수, 공백 제외 1~150자)", example = "알림이 너무 많이 와요")
        @Size(max = 2000, message = "기타 사유가 너무 깁니다.")
        String etcReason
) {

    public static final int ETC_REASON_MAX_LENGTH = 150;

    @AssertTrue(message = "기타 사유를 입력해주세요.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isEtcReasonValid() {
        if (reason != WithdrawalReason.ETC) return true;
        return etcReason != null && !etcReason.isBlank();
    }

    // 정책: 첫 글자 전 공백은 제외, 이후 공백은 포함 / 화면에 보이는 글자 단위로 집계
    @AssertTrue(message = "기타 사유는 150자 이하로 입력해주세요.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isEtcReasonWithinLimit() {
        if (reason != WithdrawalReason.ETC || etcReason == null) return true;
        return countGraphemes(etcReason.stripLeading()) <= ETC_REASON_MAX_LENGTH;
    }

    // 1자로 처리
    static int countGraphemes(String text) {
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);
        int count = 0;
        while (it.next() != BreakIterator.DONE) count++;
        return count;
    }

    //ETC가 아니면 null, ETC면 앞뒤 공백 제거
    public String normalizedEtcReason() {
        return reason == WithdrawalReason.ETC ? etcReason.strip() : null;
    }
}
