package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PostingSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostingRepository extends JpaRepository<Posting, Long> {
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.PostingSummaryResponse(p.id, p.title)
            FROM Posting p
            ORDER BY p.title ASC
            """)
    List<PostingSummaryResponse> findAllOrderByTitleAsc();

    //N+1 방지하려고 id 목록으로 회사/카테고리까지 fetch join 한 번에 하도록 했습니다.
    @Query("""
            SELECT p FROM Posting p
            JOIN FETCH p.company
            JOIN FETCH p.category
            WHERE p.id IN :ids
            """)
    List<Posting> findAllByIdInWithCompanyAndCategory(@Param("ids") List<Long> ids);
}