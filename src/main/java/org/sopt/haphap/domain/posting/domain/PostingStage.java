package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_stage_posting_order", columnList = "posting_id, order_index"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostingStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int orderIndex;

    private LocalDate expectedAnnouncementDate;

    @Column(nullable = false)
    private int expectedScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    private LocalDate announcedDate;

    // 전형 이동 새로운 정책용 (당장은 미사용, 아래 두 필드로 순차 전환 예정)
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", length = 30)
    private StageType stageType;

    // 이 전형이 "현재 전형"이 된 시각. 최소 시차·마감 판정의 기산점.
    private LocalDateTime movedAt;

    private PostingStage(String name, int orderIndex, LocalDate expectedAnnouncementDate,
                         int expectedScore, StageType stageType, Posting posting) {
        this.name = name;
        this.orderIndex = orderIndex;
        this.expectedAnnouncementDate = expectedAnnouncementDate;
        this.expectedScore = expectedScore;
        this.stageType = stageType;
        this.posting = posting;
    }

    public static PostingStage create(String name, int orderIndex, Posting posting) {
        return new PostingStage(name, orderIndex, null, 0, null, posting);
    }

    // stageType 미지정 (시더 등 기존 호출부 호환용. stageType은 나중에 별도로 채워야 함)
    public static PostingStage create(String name, int orderIndex,
                                      LocalDate expectedAnnouncementDate,int expectedScore,
                                      Posting posting) {
        return new PostingStage(name, orderIndex, expectedAnnouncementDate, expectedScore, null, posting);
    }

    public static PostingStage create(String name, int orderIndex,
                                      LocalDate expectedAnnouncementDate, int expectedScore,
                                      StageType stageType, Posting posting) {
        return new PostingStage(name, orderIndex, expectedAnnouncementDate, expectedScore, stageType, posting);
    }

    public boolean belongsTo(Posting posting) {
        return this.posting.getId().equals(posting.getId());
    }

    // 15 돌파 시 호출할 메서드 (한 번만 기록되도록 방어)
    public void markAnnouncedIfAbsent(LocalDate date) {
        if (this.announcedDate == null) {
            this.announcedDate = date;
        }
    }

    // 이 전형이 현재 전형이 된 시각 기록 (신정책 최소 시차 기산점)
    public void markMoved(LocalDateTime movedAt) {
        this.movedAt = movedAt;
    }
}