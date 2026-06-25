package org.sopt.haphap.realtime.repository;

import org.sopt.haphap.realtime.domain.RealtimeEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RealtimeEventRepository extends JpaRepository<RealtimeEvent, Long> {

    List<RealtimeEvent> findByAnnouncementIdAndIdGreaterThanOrderByIdAsc(
            Long announcementId,
            Long lastEventId,
            Pageable pageable
    );

    @Query("""
            select e
            from RealtimeEvent e
            join fetch e.announcement a
            where (:cursorId is null or e.id < :cursorId)
            order by e.id desc
            """)
    List<RealtimeEvent> findFeed(@Param("cursorId") Long cursorId, Pageable pageable);

    @Query("""
            select e
            from RealtimeEvent e
            join fetch e.announcement a
            where e.announcement.id = :announcementId
            order by e.id desc
            """)
    List<RealtimeEvent> findLatestByAnnouncementId(
            @Param("announcementId") Long announcementId,
            Pageable pageable
    );
}