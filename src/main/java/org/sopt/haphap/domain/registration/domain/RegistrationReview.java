package org.sopt.haphap.domain.registration.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

// 자동으로 판단하기 어려운 불합격 데이터를 운영진 검토 대상으로 올려둔 것.
// 검토 완료(ACCEPTED) 전까지는 집계에 반영되지 않는다 (StageResultCountUpdater 참고).
@Entity
@Getter
@Table(name = "registration_review",
        indexes = @Index(name = "idx_review_status", columnList = "status"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistrationReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationReviewReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationReviewStatus status;

    private RegistrationReview(Registration registration, RegistrationReviewReason reason) {
        this.registration = registration;
        this.reason = reason;
        this.status = RegistrationReviewStatus.PENDING;
    }

    public static RegistrationReview create(Registration registration, RegistrationReviewReason reason) {
        return new RegistrationReview(registration, reason);
    }

    public boolean isPending() {
        return this.status == RegistrationReviewStatus.PENDING;
    }

    public void accept() {
        if (!isPending()) {
            throw new IllegalStateException("이미 처리된 검토입니다: " + this.status);
        }
        this.status = RegistrationReviewStatus.ACCEPTED;
    }

    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("이미 처리된 검토입니다: " + this.status);
        }
        this.status = RegistrationReviewStatus.REJECTED;
    }
}
