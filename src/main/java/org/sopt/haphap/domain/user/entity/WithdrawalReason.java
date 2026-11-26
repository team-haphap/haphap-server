package org.sopt.haphap.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {
    //TODO: 기획에서 이 부분이 확정되면 교체하겠습니다
    JOB_PREPARATION_FINISHED("취업 준비 활동이 끝났어요."),
    NOT_ENOUGH_INFO("원하는 정보가 부족해요."),
    LOW_USAGE("서비스를 잘 사용하지 않아요."),
    PRIVACY_CONCERN("개인정보(보안) 유출이 걱정돼요."),
    ETC("기타 (직접 입력)");

    private final String description;
}
