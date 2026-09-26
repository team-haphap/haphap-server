package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.global.exception.CustomException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor

public class User extends BaseEntity {

    private static final String WITHDRAWN_DISPLAY_NAME = "탈퇴한 사용자";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String anonymousName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private LocalDate birthDate;

    private String gender;

    private String ageRange;

    private String phoneNumber;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private String appleRefreshToken;

    // 회원 탈퇴

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus withdrawalStatus = WithdrawalStatus.ACTIVE;

    private LocalDateTime withdrawalRequestedAt;

    @Builder.Default
    @Column(nullable = false)
    private int withdrawalRetryCount = 0;

    private LocalDateTime withdrawnAt;

    public void updateAppleRefreshToken(String refreshToken) {
        this.appleRefreshToken = refreshToken;
    }

    public boolean isActive() {
        return this.withdrawalStatus == WithdrawalStatus.ACTIVE;
    }

    public boolean isWithdrawn() {
        return this.withdrawalStatus == WithdrawalStatus.WITHDRAWN;
    }

    /** 1단계: 개인정보 즉시 파기. 외부 연동 해제에 필요한 providerId·appleRefreshToken만 남기기 */
    public void startWithdrawal() {
        if (!isActive()) {
            throw new CustomException(GlobalErrorCode.USER_NOT_FOUND);
        }
        this.name = WITHDRAWN_DISPLAY_NAME;
        this.email = "withdrawn+" + UUID.randomUUID() + "@deleted.local";
        this.anonymousName = "탈퇴한 사용자";
        this.birthDate = null;
        this.gender = null;
        this.ageRange = null;
        this.phoneNumber = null;
        this.withdrawalStatus = WithdrawalStatus.PENDING_UNLINK;
        this.withdrawalRequestedAt = LocalDateTime.now();
        this.withdrawalRetryCount = 0;
    }

    /** 2단계: 외부 연동 해제 성공 후 식별정보까지 파기. 여러 번 불려도 결과가 같도록 */
    public void completeWithdrawal() {
        if (this.withdrawalStatus != WithdrawalStatus.PENDING_UNLINK) {
            return;
        }
        this.providerId = "WITHDRAWN_" + UUID.randomUUID();   // 원래 ID와 연결 불가능한 값
        this.appleRefreshToken = null;
        this.withdrawalStatus = WithdrawalStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /** 연동 해제 실패 기록. 최대 횟수에 도달하면 종료 상태로 */
    public boolean recordUnlinkFailure(int maxRetry) {
        this.withdrawalRetryCount++;
        if (this.withdrawalRetryCount >= maxRetry) {
            this.withdrawalStatus = WithdrawalStatus.UNLINK_FAILED;
            return true;   // "이번에 종료 상태가 됐다" → 운영 알림은 이때 한 번만
        }
        return false;
    }
}