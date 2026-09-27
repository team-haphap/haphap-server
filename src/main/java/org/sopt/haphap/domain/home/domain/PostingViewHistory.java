package org.sopt.haphap.domain.home.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.common.BaseEntity;

// 홈 [최근 조회한 공고]. (user, posting)당 1row, 재조회 시 lastViewedAt만 갱신(upsert).
@Entity
@Getter
@Table(name = "posting_view_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_posting_view_history", columnNames = {"user_id", "posting_id"}),
        indexes = @Index(name = "idx_view_history_user_last_viewed", columnList = "user_id, last_viewed_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostingViewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    private PostingViewHistory(User user, Posting posting, LocalDateTime lastViewedAt) {
        this.user = user;
        this.posting = posting;
        this.lastViewedAt = lastViewedAt;
    }

    public static PostingViewHistory create(User user, Posting posting, LocalDateTime lastViewedAt) {
        return new PostingViewHistory(user, posting, lastViewedAt);
    }

    public void touch(LocalDateTime viewedAt) {
        this.lastViewedAt = viewedAt;
    }
}
