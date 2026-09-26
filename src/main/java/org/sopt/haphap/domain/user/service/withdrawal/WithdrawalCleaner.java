package org.sopt.haphap.domain.user.service.withdrawal;

public interface WithdrawalCleaner {
    // 탈퇴하는 회원의 도메인 개인 데이터를 정리. 트랜잭션 안에서 호출됨
    // TODO: 합격 인증 기능 구현 시 RegistrationWithdrawalCleaner 추가 (인증 이미지 S3 + URL 삭제)
    void clean(Long userId);
}