package org.sopt.haphap.domain.search.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.haphap.domain.search.domain.RelatedSearchKeyword;
import org.sopt.haphap.domain.search.dto.RelatedSearchKeywordProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelatedSearchKeywordRepository extends JpaRepository<RelatedSearchKeyword, Long> {

    @Query(value = """
        SELECT k.id AS id, k.keyword AS keyword
        FROM related_search_keyword k
        WHERE k.is_active = true
          AND k.keyword ILIKE CONCAT('%', :keyword, '%')
        ORDER BY
            CASE WHEN k.keyword = :keyword THEN 0 ELSE 1 END,
            k.keyword ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<RelatedSearchKeywordProjection> searchByKeywordContaining(
            @Param("keyword") String keyword, @Param("limit") int limit);

    // relatedKeywordId → 텍스트 변환용. 비활성화된 키워드는 조회 안 되게 isActive 조건 포함
    Optional<RelatedSearchKeyword> findByIdAndIsActiveTrue(Long id);
}