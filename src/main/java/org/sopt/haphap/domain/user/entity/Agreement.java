package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "agreement", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "type"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgreementType type;

    @Column(nullable = false)
    private boolean agreed;

    private LocalDateTime agreedAt;

    private Agreement(User user, AgreementType type, boolean agreed) {
        this.user = user;
        this.type = type;
        this.agreed = agreed;
        this.agreedAt = LocalDateTime.now();
    }

    public static Agreement create(User user, AgreementType type, boolean agreed) {
        return new Agreement(user, type, agreed);
    }
}