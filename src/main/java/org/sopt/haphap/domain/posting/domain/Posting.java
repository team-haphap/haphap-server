package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "posting", indexes = @Index(name = "idx_posting_title", columnList = "title"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting extends BaseEntity {

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
}