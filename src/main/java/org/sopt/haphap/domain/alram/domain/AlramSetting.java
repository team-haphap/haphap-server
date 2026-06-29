package org.sopt.haphap.domain.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.domain.member.domain.User;
import org.sopt.haphap.domain.posting.domain.Posting;

@Getter
@Entity
@Table(
        name = "alram_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_alram_setting_member_posting",
                columnNames = {"user_id", "posting_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlramSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @Column(nullable = false)
    private boolean enabled;

    private AlramSetting(User user, Posting posting, boolean enabled) {
        this.user = user;
        this.posting = posting;
        this.enabled = enabled;
    }

    public static AlramSetting create(User user, Posting posting, boolean enabled) {
        return new AlramSetting(user, posting, enabled);
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
