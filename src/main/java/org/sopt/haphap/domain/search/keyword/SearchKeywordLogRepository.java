package org.sopt.haphap.domain.search.keyword;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordLogRepository extends JpaRepository<SearchKeywordLog, Long> {

    @Query("""
            SELECT l.keyword AS keyword, COUNT(l) AS cnt
            FROM SearchKeywordLog l
            WHERE l.searchedAt >= :from AND l.searchedAt < :to
            GROUP BY l.keyword
            ORDER BY COUNT(l) DESC
            """)
    List<KeywordCountProjection> aggregate(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Modifying
    @Query("DELETE FROM SearchKeywordLog l WHERE l.searchedAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}