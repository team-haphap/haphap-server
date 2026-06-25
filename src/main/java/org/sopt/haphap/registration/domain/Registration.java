package org.sopt.haphap.registration.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.member.domain.Member;
import org.sopt.haphap.posting.domain.Posting;

@Getter
@Entity
@Table(name = "registration")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String stage;               // 서류, 코테, 1차면접 등등 (공고마다 다른 전형명)

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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Registration(Member member, Posting posting, String stage, RegistrationResult result,
                         ContactMethod contactMethod, LocalDate contactedAt, boolean anonymous) {
        this.member = member;
        this.posting = posting;
        this.stage = stage;
        this.result = result;
        this.contactMethod = contactMethod;
        this.contactedAt = contactedAt;
        this.anonymous = anonymous;
    }

    public static Registration create(Member member, Posting posting, String stage,
                                      RegistrationResult result, ContactMethod contactMethod,
                                      LocalDate contactedAt, boolean anonymous) {
        return new Registration(member, posting, stage, result, contactMethod, contactedAt, anonymous);
    }
}