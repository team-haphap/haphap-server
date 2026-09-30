package org.sopt.haphap.domain.home.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.sopt.haphap.domain.home.domain.PostingViewHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostingViewHistoryRepository extends JpaRepository<PostingViewHistory, Long> {

    Optional<PostingViewHistory> findByUserIdAndPostingId(Long userId, Long postingId);

    // 최근 조회순, 30일 이내만. row는 안 지우고 조회 시점에만 필터링.
    @Query("""
        SELECT v.posting.id
        FROM PostingViewHistory v
        WHERE v.user.id = :userId AND v.lastViewedAt >= :since
        ORDER BY v.lastViewedAt DESC
        """)
    List<Long> findRecentPostingIds(
            @Param("userId") Long userId, @Param("since") LocalDateTime since, Pageable pageable);
}
