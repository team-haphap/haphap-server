package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.PostingActivityProjection;
import org.sopt.haphap.domain.posting.dto.PostingSummaryResponse;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
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

    /**
     * 1."활동 있는 공고 + 활동시각"을 한 방에 집계.
     * Registration을 공고별로 GROUP BY 해서 MAX(updatedAt)을 활동시각으로 뽑고, 그 순으로 정렬한 공고 id를 가져옴.
     * 카테고리도 여기서 필터링
     */
    @Query("""
        SELECT r.posting.id AS postingId, MAX(r.updatedAt) AS activityAt
        FROM Registration r
        WHERE (:categoryNames IS NULL OR r.posting.category.name IN :categoryNames)
        GROUP BY r.posting.id
        ORDER BY MAX(r.updatedAt) DESC
        """)
    List<PostingActivityProjection> findActivePostingIdsByCategories(
            @Param("categoryNames") List<String> categoryNames);

    //2. 뽑은 공고 id들로 필요한 데이터를 배치 조회
    //2-1 공고+회사+카테고리  fetch join으로 한 번에 (N+1 방지)
    @Query("""
        SELECT p FROM Posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE p.id IN :ids
        """)
    List<Posting> findAllWithCompanyAndCategoryByIds(@Param("ids") List<Long> ids);

}