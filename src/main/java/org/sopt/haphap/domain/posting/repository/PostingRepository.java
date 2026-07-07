package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PostingAutocompleteProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostingRepository extends JpaRepository<Posting, Long> {

    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse(p.id, p.title)
            FROM Posting p
            ORDER BY p.title ASC
            """)
    List<PostingSummaryResponse> findAllOrderByTitleAsc();

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

    @Query("""
            SELECT p FROM Posting p
            JOIN FETCH p.company
            JOIN FETCH p.category
            WHERE p.id = :postingId
            """)
    Optional<Posting> findWithCompanyAndCategory(@Param("postingId") Long postingId);

    @Query(value = """
            SELECT p.id AS id, p.title AS title
            FROM posting p
            WHERE p.title ILIKE CONCAT('%', :keyword, '%')
              AND (p.deadline IS NULL OR p.deadline >= CURRENT_DATE)
            ORDER BY similarity(p.title, :keyword) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<PostingAutocompleteProjection> searchByTitleContaining(
            @Param("keyword") String keyword, @Param("limit") int limit);

    // 캘린더 카드는 title만 필요 - 불필요한 fetch join 없이 최소 필드만 배치 조회하도록!
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse(p.id, p.title)
            FROM Posting p
            WHERE p.id IN :ids
            """)
    List<PostingSummaryResponse> findSummariesByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT p.id FROM Posting p
            JOIN p.company c
            JOIN p.category cat
            WHERE (:keyword IS NULL
                    OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:categories IS NULL OR cat.name IN :categories)
              AND (:status IS NULL
                    OR (:status = 'open' AND (p.deadline IS NULL OR p.deadline >= CURRENT_DATE))
                    OR (:status = 'closed' AND p.deadline < CURRENT_DATE))
            """)
    List<Long> searchPostingIds(
            @Param("keyword") String keyword,
            @Param("categories") List<String> categories,
            @Param("status") String status);
}