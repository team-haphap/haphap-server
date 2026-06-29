package org.sopt.haphap.domain.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "alram_failure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlramFailure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postingId;

    @Column(nullable = false)
    private Long registrantMemberId;

    @Column(length = 50)
    private String stage;

    @Column(nullable = false, length = 100)
    private String failureType;     // 예외 클래스명

    @Column(length = 1000)
    private String failureMessage;  // 예외 메시지

    @Column(nullable = false)
    private boolean resolved;       // 재처리 완료 여부

    private AlramFailure(Long postingId, Long registrantMemberId, String stage,
                         String failureType, String failureMessage) {
        this.postingId = postingId;
        this.registrantMemberId = registrantMemberId;
        this.stage = stage;
        this.failureType = failureType;
        this.failureMessage = failureMessage;
        this.resolved = false;
    }

    public static AlramFailure from(Long postingId, Long registrantMemberId, String stage, Throwable e) {
        String message = e.getMessage();
        if (message != null && message.length() > 1000) {
            message = message.substring(0, 1000);   // 컬럼 길이 초과 방지
        }
        return new AlramFailure(postingId, registrantMemberId, stage,
                e.getClass().getSimpleName(), message);
    }

    public void markResolved() {
        this.resolved = true;
    }
}