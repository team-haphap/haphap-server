package org.sopt.haphap.domain.user.service.withdrawal;

public interface WithdrawalCleaner {
    // 탈퇴하는 회원의 도메인 개인 데이터를 정리. 트랜잭션 안에서 호출됨
    void clean(Long userId);
}