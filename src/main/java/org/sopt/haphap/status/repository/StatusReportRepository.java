package org.sopt.haphap.status.repository;

import org.sopt.haphap.status.domain.StatusReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusReportRepository extends JpaRepository<StatusReport, Long> {

    long countByAnnouncementId(Long announcementId);
}