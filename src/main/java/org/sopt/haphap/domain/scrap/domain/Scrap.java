package org.sopt.haphap.domain.scrap.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.common.BaseEntity;

// 찜 생성은 동시 요청 멱등 처리를 위해 ScrapRepository.insertIfAbsent(네이티브 INSERT ON CONFLICT)로만 한다.
@Getter
@Entity
@Table(name = "scrap",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_scrap_user_posting",
                columnNames = {"user_id", "posting_id"}),
        indexes = @Index(name = "idx_scrap_user_created", columnList = "user_id, created_at DESC"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;
}
