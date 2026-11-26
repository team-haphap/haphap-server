package org.sopt.haphap.domain.content.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "content", indexes = {
        @Index(name = "idx_content_type_id", columnList = "type, id"),
        @Index(name = "idx_content_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 탈퇴 시 비식별 유지를 위해 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 후기 대상 공고
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id")
    private Posting posting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private Content(User user, Posting posting, ContentType type, String title, String body) {
        this.user = user;
        this.posting = posting;
        this.type = type;
        this.title = title;
        this.body = body;
    }

    public static Content create(User user, Posting posting, ContentType type, String title, String body) {
        return new Content(user, posting, type, title, body);
    }

    public boolean isWrittenBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }
}