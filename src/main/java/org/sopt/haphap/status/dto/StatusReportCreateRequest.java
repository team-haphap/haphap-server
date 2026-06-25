package org.sopt.haphap.status.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.sopt.haphap.announcement.domain.RecruitmentStage;
import org.sopt.haphap.status.domain.NotificationChannel;
import org.sopt.haphap.status.domain.StatusResult;

public record StatusReportCreateRequest(
        @NotNull @Positive Long announcementId,
        @NotNull RecruitmentStage stage,
        @NotNull LocalDate notifiedDate,
        @NotBlank String notifiedTimeText,
        @NotNull NotificationChannel notificationChannel,
        @NotNull StatusResult result
) {
}