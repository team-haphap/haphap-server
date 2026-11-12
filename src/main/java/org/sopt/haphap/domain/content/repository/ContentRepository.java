package org.sopt.haphap.domain.content.repository;

import java.util.Optional;
import org.sopt.haphap.domain.content.domain.Content;
import org.sopt.haphap.domain.content.domain.ContentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, Long> {

    // 탐색 목록
    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.user
        LEFT JOIN FETCH c.posting p
        LEFT JOIN FETCH p.company
        WHERE c.type = :type
        ORDER BY c.id DESC
        """)
    Slice<Content> findSliceByType(@Param("type") ContentType type, Pageable pageable);

    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.user
        LEFT JOIN FETCH c.posting p
        LEFT JOIN FETCH p.company
        ORDER BY c.id DESC
        """)
    Slice<Content> findSliceAll(Pageable pageable);

    // 상세 열람
    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.user
        LEFT JOIN FETCH c.posting p
        LEFT JOIN FETCH p.company
        WHERE c.id = :id
        """)
    Optional<Content> findDetailById(@Param("id") Long id);

    // 작성한 콘텐츠
    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.posting p
        LEFT JOIN FETCH p.company
        WHERE c.user.id = :userId
        ORDER BY c.id DESC
        """)
    Slice<Content> findSliceByUserId(@Param("userId") Long userId, Pageable pageable);

    // 탈퇴 시 비식별화: 작성자 연결만 끊고 글은 유지
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Content c SET c.user = null WHERE c.user.id = :userId")
    int detachAllByUserId(@Param("userId") Long userId);
}