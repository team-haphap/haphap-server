package org.sopt.haphap.domain.registration.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.posting.domain.Posting;

@Getter
@Entity
@Table(name = "registration",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_registration_user_posting_stage",
                columnNames = {"user_id", "posting_id", "stage_id"}),
        indexes = {
                @Index(name = "idx_reg_result_updated", columnList = "result, updated_at"),
                @Index(name = "idx_reg_posting_stage", columnList = "posting_id, stage_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private PostingStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationResult result;  // 합격 / 불합격 / 대기

    // 합격 인증(인증샷 검토) 상태. 실제 승인/반려 플로우는 별도 파트에서 연결 예정이라
    // 지금은 전형 이동 트리거가 참조할 상태값만 갖고 있다.
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private RegistrationVerificationStatus verificationStatus;

    @Convert(converter = ContactMethodListConverter.class)
    @Column(name = "contact_methods")
    private List<ContactMethod> contactMethods;

    private LocalDateTime contactedAt;

    @Column(nullable = false)
    private boolean anonymous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Registration(User user, Posting posting, PostingStage stage, RegistrationResult result,
                         List<ContactMethod> contactMethods, LocalDateTime contactedAt, boolean anonymous) {
        this.user = user;
        this.posting = posting;
        this.stage = stage;
        this.result = result;
        this.contactMethods = contactMethods;
        this.contactedAt = contactedAt;
        this.anonymous = anonymous;
        this.verificationStatus = deriveVerificationStatus(result);
    }

    // PASS만 인증 대상. FAIL/PENDING은 애초에 검토할 게 없어 NOT_REQUIRED로 바로 확정.
    private static RegistrationVerificationStatus deriveVerificationStatus(RegistrationResult result) {
        return result == RegistrationResult.PASS
                ? RegistrationVerificationStatus.PENDING
                : RegistrationVerificationStatus.NOT_REQUIRED;
    }

    public boolean isPending() {
        return this.result == RegistrationResult.PENDING;
    }

    public boolean isPass() {
        return this.result == RegistrationResult.PASS;
    }

    public boolean hasSameResult(RegistrationResult other) {
        return this.result == other;
    }

    public static Registration create(User user, Posting posting, PostingStage stage,
                                      RegistrationResult result, List<ContactMethod> contactMethods,
                                      LocalDateTime contactedAt, boolean anonymous) {
        return new Registration(user, posting, stage, result, contactMethods, contactedAt, anonymous);
    }


    // 기존 등록을 새 값으로 갱신 (force 재요청 시)
    public void updateRegistration(RegistrationResult result, List<ContactMethod> contactMethods,
                                   LocalDateTime contactedAt, boolean anonymous) {
        this.result = result;
        this.contactMethods = contactMethods;
        this.contactedAt = contactedAt;
        this.anonymous = anonymous;
        this.verificationStatus = deriveVerificationStatus(result);
    }

    // 운영진 승인/반려 — 실제 트리거(인증샷 검토 UI 등)는 별도 파트에서 연결 예정
    public void approve() {
        if (this.verificationStatus != RegistrationVerificationStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닙니다: " + this.verificationStatus);
        }
        this.verificationStatus = RegistrationVerificationStatus.APPROVED;
    }

    public void reject() {
        if (this.verificationStatus != RegistrationVerificationStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닙니다: " + this.verificationStatus);
        }
        this.verificationStatus = RegistrationVerificationStatus.REJECTED;
    }

    public boolean isApproved() {
        return this.verificationStatus == RegistrationVerificationStatus.APPROVED;
    }
}