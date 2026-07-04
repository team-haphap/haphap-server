package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse;
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

    //공고+회사+카테고리  fetch join으로 한 번에 (N+1 방지)
    @Query("""
        SELECT p FROM Posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE p.id IN :ids
        """)
    List<Posting> findAllWithCompanyAndCategoryByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT p FROM Posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE (:categoryNames IS NULL OR p.category.name IN :categoryNames)
        """)
    List<Posting> findAllWithCompanyAndCategory(@Param("categoryNames") List<String> categoryNames);
}