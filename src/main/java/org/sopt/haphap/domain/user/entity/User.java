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

    private LocalDateTime withdrawnAt;

    private String appleRefreshToken;

    public void updateAppleRefreshToken(String refreshToken) {
        this.appleRefreshToken = refreshToken;
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus withdrawalStatus = WithdrawalStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private int withdrawalRetryCount = 0;

    private LocalDateTime withdrawalRequestedAt;

    public void markPendingUnlink() {
        this.withdrawalStatus = WithdrawalStatus.PENDING_UNLINK;
        this.withdrawalRequestedAt = LocalDateTime.now();
    }

    public void incrementWithdrawalRetryCount() {
        this.withdrawalRetryCount++;
    }

    public void withdraw() {
        if (this.withdrawalStatus == WithdrawalStatus.WITHDRAWN) {
            throw new CustomException(GlobalErrorCode.USER_NOT_FOUND); // 이미 탈퇴한 회원
        }
        //개인정보 파기 코드
        this.name = "탈퇴한 사용자";
        this.email = "withdrawn+" + UUID.randomUUID() + "@deleted.local";
        this.anonymousName = "탈퇴한 사용자";
        this.birthDate = null;
        this.gender = null;
        this.ageRange = null;
        this.phoneNumber = null;
        //소셜 식별정보도 파기해서 같은 카카오 계정으로 다시 가입하면 새 회원으로 생성될 수 있도록 함
        this.providerId = "WITHDRAWN_" + UUID.randomUUID();
        this.appleRefreshToken = null;
        this.withdrawalStatus = WithdrawalStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
}