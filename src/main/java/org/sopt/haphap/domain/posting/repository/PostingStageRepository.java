package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.PostingStageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostingStageRepository extends JpaRepository<PostingStage, Long> {
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.PostingStageResponse(s.id, s.name, s.orderIndex)
            FROM PostingStage s
            WHERE s.posting.id = :postingId
            ORDER BY s.orderIndex ASC
            """)
    List<PostingStageResponse> findStagesByPostingId(@Param("postingId") Long postingId);
}
