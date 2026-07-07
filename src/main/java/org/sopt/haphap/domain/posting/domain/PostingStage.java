package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    private PostingStage(String name, int orderIndex, LocalDate expectedAnnouncementDate,
                         int expectedScore, Posting posting) {
        this.name = name;
        this.orderIndex = orderIndex;
        this.expectedAnnouncementDate = expectedAnnouncementDate;
        this.expectedScore = expectedScore;
        this.posting = posting;
    }

    public static PostingStage create(String name, int orderIndex, Posting posting) {
        return new PostingStage(name, orderIndex, null, 0,posting);
    }

    public static PostingStage create(String name, int orderIndex,
                                      LocalDate expectedAnnouncementDate,int expectedScore,
                                      Posting posting) {
        return new PostingStage(name, orderIndex, expectedAnnouncementDate,expectedScore, posting);
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
}