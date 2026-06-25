package org.sopt.haphap.status.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.announcement.domain.Announcement;
import org.sopt.haphap.announcement.domain.RecruitmentStage;
import org.sopt.haphap.global.common.BaseEntity;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "status_report",
        indexes = {
                @Index(name = "idx_status_report_announcement_created", columnList = "announcement_id, created_at"),
                @Index(name = "idx_status_report_member_announcement", columnList = "member_id, announcement_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecruitmentStage stage;

    @Column(name = "notified_date", nullable = false)
    private LocalDate notifiedDate;

    @Column(name = "notified_time_text", nullable = false, length = 20)
    private String notifiedTimeText;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel", nullable = false, length = 20)
    private NotificationChannel notificationChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusResult result;

    private StatusReport(
            Long memberId,
            Announcement announcement,
            RecruitmentStage stage,
            LocalDate notifiedDate,
            String notifiedTimeText,
            NotificationChannel notificationChannel,
            StatusResult result
    ) {
        this.memberId = memberId;
        this.announcement = announcement;
        this.stage = stage;
        this.notifiedDate = notifiedDate;
        this.notifiedTimeText = notifiedTimeText;
        this.notificationChannel = notificationChannel;
        this.result = result;
    }

    public static StatusReport create(
            Long memberId,
            Announcement announcement,
            RecruitmentStage stage,
            LocalDate notifiedDate,
            String notifiedTimeText,
            NotificationChannel notificationChannel,
            StatusResult result
    ) {
        return new StatusReport(memberId, announcement, stage, notifiedDate, notifiedTimeText, notificationChannel, result);
    }
}