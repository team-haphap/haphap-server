package org.sopt.haphap.domain.passcard.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PassCardSuccessCode implements SuccessResultCode {

    PASS_CARD_UNCONFIRMED_FETCHED(HttpStatus.OK, "미확인 합격카드 조회에 성공했습니다."),
    PASS_CARD_LIST_FETCHED(HttpStatus.OK, "합격카드 보관함 조회에 성공했습니다."),
    PASS_CARD_FETCHED(HttpStatus.OK, "합격카드 조회에 성공했습니다."),
    PASS_CARD_CONFIRMED(HttpStatus.NO_CONTENT, "합격카드 확인 처리가 완료되었습니다.");

    private final HttpStatus status;
    private final String message;
}