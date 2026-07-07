package org.sopt.haphap.domain.registration.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ContactMethod contactMethod;

    private LocalDate contactedAt;

    @Column(nullable = false)
    private boolean anonymous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Registration(User user, Posting posting, PostingStage stage, RegistrationResult result,
                         ContactMethod contactMethod, LocalDate contactedAt, boolean anonymous) {
        this.user = user;
        this.posting = posting;
        this.stage = stage;
        this.result = result;
        this.contactMethod = contactMethod;
        this.contactedAt = contactedAt;
        this.anonymous = anonymous;
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
                                      RegistrationResult result, ContactMethod contactMethod,
                                      LocalDate contactedAt, boolean anonymous) {
        return new Registration(user, posting, stage, result, contactMethod, contactedAt, anonymous);
    }


    // 기존 등록을 새 값으로 갱신 (force 재요청 시)
    public void updateRegistration(RegistrationResult result, ContactMethod contactMethod,
                                   LocalDate contactedAt, boolean anonymous) {
        this.result = result;
        this.contactMethod = contactMethod;
        this.contactedAt = contactedAt;
        this.anonymous = anonymous;
    }
}