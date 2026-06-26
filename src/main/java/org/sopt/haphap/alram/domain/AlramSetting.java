package org.sopt.haphap.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.member.domain.Member;
import org.sopt.haphap.posting.domain.Posting;

@Getter
@Entity
@Table(
        name = "alram_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_alram_setting_member_posting",
                columnNames = {"member_id", "posting_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlramSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @Column(nullable = false)
    private boolean enabled;

    private AlramSetting(Member member, Posting posting, boolean enabled) {
        this.member = member;
        this.posting = posting;
        this.enabled = enabled;
    }

    public static AlramSetting create(Member member, Posting posting, boolean enabled) {
        return new AlramSetting(member, posting, enabled);
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
