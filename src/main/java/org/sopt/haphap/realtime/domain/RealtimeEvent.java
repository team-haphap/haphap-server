package org.sopt.haphap.realtime.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.announcement.domain.Announcement;
import org.sopt.haphap.announcement.domain.RecruitmentStage;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.status.domain.StatusResult;

@Getter
@Entity
@Table(
        name = "realtime_event",
        indexes = {
                @Index(name = "idx_realtime_event_announcement_id", columnList = "announcement_id"),
                @Index(name = "idx_realtime_event_created_id", columnList = "created_at, id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealtimeEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private RealtimeEventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecruitmentStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusResult result;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 160)
    private String body;

    private RealtimeEvent(
            Announcement announcement,
            RealtimeEventType type,
            RecruitmentStage stage,
            StatusResult result,
            String title,
            String body
    ) {
        this.announcement = announcement;
        this.type = type;
        this.stage = stage;
        this.result = result;
        this.title = title;
        this.body = body;
    }

    public static RealtimeEvent statusReportCreated(
            Announcement announcement,
            RecruitmentStage stage,
            StatusResult result
    ) {
        String title = result.getDisplayName() + " 등록!";
        String body = "익명의 지원자가 방금 " + result.getDisplayName() + ".";
        return new RealtimeEvent(
                announcement,
                RealtimeEventType.STATUS_REPORT_CREATED,
                stage,
                result,
                title,
                body
        );
    }
}