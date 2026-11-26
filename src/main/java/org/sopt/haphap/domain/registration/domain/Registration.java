package org.sopt.haphap.domain.registration.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.global.exception.CustomException;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private RegistrationVerificationStatus verificationStatus;

    @Column(name = "pass_card_confirmed_at")
    private LocalDateTime passCardConfirmedAt;

    @Column(name = "verification_round", nullable = false)
    private int verificationRound = 1;

    @Column(name = "verification_submitted_at")
    private LocalDateTime verificationSubmittedAt;

    @Column(name = "verification_resolved_at")
    private LocalDateTime verificationResolvedAt;

    //반려 사유 추가
    @Column(name = "verification_reject_reason", length = 500)
    private String verificationRejectReason;

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
        applyVerificationStatus(result);
    }

    // PASS만 인증 대상. FAIL/PENDING은 애초에 검토할 게 없어 NOT_REQUIRED로 바로 확정.
    private static RegistrationVerificationStatus deriveVerificationStatus(RegistrationResult result) {
        return result == RegistrationResult.PASS
                ? RegistrationVerificationStatus.PENDING
                : RegistrationVerificationStatus.NOT_REQUIRED;
    }

    private void applyVerificationStatus(RegistrationResult result) {
        this.verificationStatus = deriveVerificationStatus(result);
        this.verificationSubmittedAt =
                this.verificationStatus == RegistrationVerificationStatus.PENDING ? LocalDateTime.now() : null;
        this.verificationResolvedAt = null;
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
        applyVerificationStatus(result);
    }

    public void approve() {
        transitionTo(RegistrationVerificationStatus.APPROVED, RegistrationErrorCode.VERIFICATION_ALREADY_RESOLVED);
        this.verificationResolvedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        transitionTo(RegistrationVerificationStatus.REJECTED, RegistrationErrorCode.VERIFICATION_ALREADY_RESOLVED);
        this.verificationResolvedAt = LocalDateTime.now();
        this.verificationRejectReason = reason;
    }

    public void resubmitVerification() {
        transitionTo(RegistrationVerificationStatus.PENDING, RegistrationErrorCode.VERIFICATION_NOT_RESUBMITTABLE);
        this.verificationRound++;
        this.verificationSubmittedAt = LocalDateTime.now();
        this.verificationResolvedAt = null;
        this.verificationRejectReason = null;
    }

    public boolean isVerificationResubmittable() {
        return this.verificationStatus == RegistrationVerificationStatus.REJECTED;
    }

    private void transitionTo(RegistrationVerificationStatus next, ErrorResultCode onFail) {
        if (!this.verificationStatus.canTransitionTo(next)) {
            throw new CustomException(onFail);
        }
        this.verificationStatus = next;
    }

    //합격카드가 사용자 화면에 정상 노출된 시점 기록
    public void confirmPassCard() {
        if (this.passCardConfirmedAt == null) {
            this.passCardConfirmedAt = LocalDateTime.now();
        }
    }

    public boolean isPassCardConfirmed() {
        return this.passCardConfirmedAt != null;
    }

    public boolean isApproved() {
        return this.verificationStatus == RegistrationVerificationStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.verificationStatus == RegistrationVerificationStatus.REJECTED;
    }
}