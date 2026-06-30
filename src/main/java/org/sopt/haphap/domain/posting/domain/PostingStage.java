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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    private PostingStage(String name, int orderIndex, LocalDate expectedAnnouncementDate, Posting posting) {
        this.name = name;
        this.orderIndex = orderIndex;
        this.expectedAnnouncementDate = expectedAnnouncementDate;
        this.posting = posting;
    }

    public static PostingStage create(String name, int orderIndex, Posting posting) {
        return new PostingStage(name, orderIndex, null, posting);
    }

    public static PostingStage create(String name, int orderIndex,
                                      LocalDate expectedAnnouncementDate, Posting posting) {
        return new PostingStage(name, orderIndex, expectedAnnouncementDate, posting);
    }

    public boolean belongsTo(Posting posting) {
        return this.posting.getId().equals(posting.getId());
    }
}