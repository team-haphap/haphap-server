package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "posting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    private LocalDate deadline;

    private LocalDate expectedAnnouncementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;


    private Posting(String title, LocalDate deadline, Category category, Company company) {
        this.title = title;
        this.deadline = deadline;
        this.category = category;
        this.company = company;
    }

    public static Posting create(String title, LocalDate deadline, Category category, Company company) {
        return new Posting(title, deadline, category, company);
    }
}