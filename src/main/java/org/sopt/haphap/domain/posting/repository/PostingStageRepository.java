package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingStageResponse;
import org.sopt.haphap.domain.posting.dto.projection.TodayAnnouncementProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PostingStageRepository extends JpaRepository<PostingStage, Long> {
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.PostingStageResponse(s.id, s.name, s.orderIndex)
            FROM PostingStage s
            WHERE s.posting.id = :postingId
            ORDER BY s.orderIndex ASC
            """)
    List<PostingStageResponse> findStagesByPostingId(@Param("postingId") Long postingId);

    @Query("""
        SELECT s.posting.id AS postingId, s.id AS stageId,
               s.name AS name, s.orderIndex AS orderIndex,
               s.expectedAnnouncementDate AS expectedAnnouncementDate
        FROM PostingStage s
        WHERE s.posting.id IN :postingIds
        ORDER BY s.posting.id ASC, s.orderIndex ASC
        """)
    List<PostingStageFlatProjection> findFlatByPostingIds(@Param("postingIds") List<Long> postingIds);

    @Query("""
        SELECT s.id AS stageId, s.name AS stageName, s.expectedScore AS expectedScore,
               p.id AS postingId, p.title AS title,
               c.name AS companyName, cat.name AS categoryName,
               c.imageUrl AS imageUrl
        FROM PostingStage s
        JOIN s.posting p
        JOIN p.company c
        JOIN p.category cat
        WHERE s.expectedAnnouncementDate = :today
        ORDER BY s.expectedScore DESC
        """)
    List<TodayAnnouncementProjection> findTodayAnnouncements(@Param("today") LocalDate today, Pageable pageable);
}
