package org.sopt.haphap.announcement.repository;

import org.sopt.haphap.announcement.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}