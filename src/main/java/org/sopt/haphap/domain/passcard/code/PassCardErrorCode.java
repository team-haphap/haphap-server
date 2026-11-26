package org.sopt.haphap.domain.passcard.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PassCardErrorCode implements ErrorResultCode {

    // 등록이 없거나, 본인 것이 아니거나, 아직 승인되지 않은 경우 모두 동일하게
    PASS_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "합격카드를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}