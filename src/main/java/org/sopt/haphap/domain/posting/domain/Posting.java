package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "posting", indexes = @Index(name = "idx_posting_title", columnList = "title"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting extends BaseEntity {

    // 마감 정책(transfer_new_new.md): 최종합격 전형 이동일 + 4일이 되는 날 00:00에 자동 마감.
    private static final int CLOSE_AFTER_DAYS = 4;

    // D-day 칩 라벨
    private static final String D_DAY_LABEL = "D-day";
    private static final String CHECKING_LABEL = "발표 확인 중";
    private static final String CLOSED_LABEL = "마감";
    private static final String UPCOMING_LABEL = "진행 예정";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200,
            columnDefinition = "varchar(200) COLLATE \"ko-KR-x-icu\"")
    private String title;

    private LocalDate deadline;

    private LocalDate expectedAnnouncementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 전형 이동 새로운 정책용. 공고 등록 시 첫 전형으로 초기화되고
    // 이동 조건 충족 시에만 한 단계씩 전진하는 영속 상태 포인터.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id")
    private PostingStage currentStage;

    private String location;

    private String position;

    private Posting(String title, LocalDate deadline, String location, String position,Category category, Company company) {
        this.title = title;
        this.deadline = deadline;
        this.location = location;
        this.position = position;
        this.category = category;
        this.company = company;
    }

    public static Posting create(String title, LocalDate deadline,  String location, String position,Category category, Company company) {
        return new Posting(title, deadline,location,position, category, company);
    }

    public void update(String title, LocalDate deadline, String location, String position,
                       Category category, Company company) {
        this.title = title;
        this.deadline = deadline;
        this.location = location;
        this.position = position;
        this.category = category;
        this.company = company;
    }

    // 전형 이동 신정책: 이동 조건 충족 시 현재 전형 포인터를 한 단계 전진
    public void moveCurrentStageTo(PostingStage nextStage) {
        this.currentStage = nextStage;
    }

    // (예: 9/23 최종합격 이동 → 9/27 00:00 마감)
    public boolean isClosed() {
        if (currentStage == null || currentStage.getStageType() != StageType.FINAL_PASS) {
            return false;
        }
        LocalDateTime movedAt = currentStage.getMovedAt();
        if (movedAt == null) {
            return false;   // 이동 시각 미기록 → 안전하게 유예
        }
        LocalDateTime closesAt = movedAt.toLocalDate().plusDays(CLOSE_AFTER_DAYS).atStartOfDay();
        return !LocalDateTime.now().isBefore(closesAt);
    }

    // D-day 칩 정책: 현재 전형의 예상 발표일 기준으로 라벨을 정한다.
    // 전형이 바뀌면 이동된 전형을 다시 읽으므로 재계산은 별도 로직 없이 항상 최신 기준으로 이루어진다.
    public StageDisplay resolveDisplay() {
        if (isClosed()) {
            return new StageDisplay(null, CLOSED_LABEL, true);
        }
        if (currentStage == null) {
            return new StageDisplay(null, UPCOMING_LABEL, false);   // 아직 전형 미등록 → 초기화 전
        }

        LocalDate expected = currentStage.getExpectedAnnouncementDate();
        if (expected == null) {
            return new StageDisplay(currentStage, null, false);   // 예정일 미입력
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(expected)) {
            long daysUntil = ChronoUnit.DAYS.between(today, expected);
            return new StageDisplay(currentStage, "D-" + daysUntil, false);
        }
        if (today.isEqual(expected)) {
            return new StageDisplay(currentStage, D_DAY_LABEL, false);
        }
        return new StageDisplay(currentStage, CHECKING_LABEL, false);   // 예정일은 지났는데 아직 이동 전
    }

    public record StageDisplay(PostingStage stage, String label, boolean closed) {}
}