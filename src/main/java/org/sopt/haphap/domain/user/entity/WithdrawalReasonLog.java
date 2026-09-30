package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Entity
@Table(name = "withdrawal_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalReasonLog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WithdrawalReason reason;

    @Column(length = 200)
    private String etcReason;

    private WithdrawalReasonLog(WithdrawalReason reason, String etcReason) {
        this.reason = reason;
        this.etcReason = etcReason;
    }

    public static WithdrawalReasonLog of(WithdrawalReason reason, String etcReason) {
        return new WithdrawalReasonLog(reason, etcReason);
    }
}