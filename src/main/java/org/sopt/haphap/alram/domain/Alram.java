package org.sopt.haphap.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.member.domain.User;
import org.sopt.haphap.posting.domain.Posting;

@Getter
@Entity
@Table(name = "alram")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alram extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlramType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 255)
    private String body;

    @Column(nullable = false)
    private boolean isRead;

    private Alram(User receiver, Posting posting, String title, String body) {
        this.receiver = receiver;
        this.posting = posting;
        this.title = title;
        this.body = body;
        this.isRead = false;
    }

    private Alram(User user, Posting posting, AlramType type, String title, String body) {
        this.receiver = user;
        this.posting = posting;
        this.type = type;
        this.title = title;
        this.body = body;
        this.isRead = false;
    }

    public static Alram newStageRegistration(User user, Posting posting, String stageName) {
        return new Alram(
                user,
                posting,
                AlramType.NEW_STAGE_REGISTRATION,
                "새 전형 제보가 등록됐어요",
                posting.getTitle() + " 공고에 " + stageName + " 전형 제보가 올라왔어요."
        );
    }

    public static Alram create(User receiver, Posting posting, String title, String body) {
        return new Alram(receiver, posting, title, body);
    }

    public static Alram create(User receiver, Posting posting, AlramType type, String title, String body) {
        return new Alram(receiver, posting, type, title, body);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}