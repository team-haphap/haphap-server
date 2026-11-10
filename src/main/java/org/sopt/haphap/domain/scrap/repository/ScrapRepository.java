package org.sopt.haphap.domain.scrap.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.scrap.domain.Scrap;
import org.sopt.haphap.domain.scrap.projection.MyScrapProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 조회 후 저장하면 동시 요청이 둘 다 "없음"을 보고 INSERT → 한쪽이 유니크 위반으로 실패한다.
    // 한 문장으로 원자적으로 처리하고, 반환값(1: 새로 찜함 / 0: 이미 찜해 있음)으로 결과를 구분한다.
    @Modifying
    @Query(value = """
        INSERT INTO scrap (user_id, posting_id, created_at, updated_at)
        VALUES (:userId, :postingId, :now, :now)
        ON CONFLICT (user_id, posting_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("postingId") Long postingId,
                       @Param("now") LocalDateTime now);

    // 마이페이지 [찜한 공고]: 찜한 시각 최신순, 같으면 공고 id 역순. 취소 후 다시 찜하면 행이 새로 생겨 다시 찜한 시각이 기준이 된다.
    // company_image는 (company, type) 유니크라 LEFT JOIN해도 찜 한 건당 한 행 → 로고까지 쿼리 1번.
    @Query("""
        SELECT p.id AS postingId, c.name AS companyName, cat.name AS categoryName,
               p.title AS title, ci.imageUrl AS logoImageUrl
        FROM Scrap s
        JOIN s.posting p
        JOIN p.company c
        JOIN p.category cat
        LEFT JOIN CompanyImage ci ON ci.company = c AND ci.type = :imageType
        WHERE s.user.id = :userId
        ORDER BY s.createdAt DESC, p.id DESC
        """)
    List<MyScrapProjection> findMyScraps(@Param("userId") Long userId,
                                         @Param("imageType") CompanyImageType imageType);

    // DELETE는 그 자체로 멱등이라 동시 요청이 와도 한쪽만 1, 나머지는 0을 받는다.
    @Modifying
    @Query("DELETE FROM Scrap s WHERE s.user.id = :userId AND s.posting.id = :postingId")
    int deleteByUserIdAndPostingId(@Param("userId") Long userId, @Param("postingId") Long postingId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Scrap s WHERE s.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
