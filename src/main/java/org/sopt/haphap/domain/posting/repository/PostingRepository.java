package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PostingSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostingRepository extends JpaRepository<Posting, Long> {
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.PostingSummaryResponse(p.id, p.title)
            FROM Posting p
            ORDER BY p.title ASC
            """)
    List<PostingSummaryResponse> findAllOrderByTitleAsc();
}