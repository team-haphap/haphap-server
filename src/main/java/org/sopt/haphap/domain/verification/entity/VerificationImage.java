package org.sopt.haphap.domain.verification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.verification.code.VerificationErrorCode;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.global.exception.CustomException;

@Entity
@Table(name = "verification_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Registration registration;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    private Integer sortOrder;

    private VerificationImage(User user, String s3Key) {
        this.user = user;
        this.s3Key = s3Key;
    }

    public static VerificationImage uploadedBy(User user, String s3Key) {
        return new VerificationImage(user, s3Key);
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isAttached() {
        return this.registration != null;
    }

    public void attachTo(Registration registration, int sortOrder) {
        if (isAttached()) {
            throw new CustomException(VerificationErrorCode.IMAGE_ALREADY_ATTACHED);
        }
        this.registration = registration;
        this.sortOrder = sortOrder;
    }
}