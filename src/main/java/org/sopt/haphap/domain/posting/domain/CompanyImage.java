package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Entity
@Getter
@Table(
        name = "company_image",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_image_company_type",
                columnNames = {"company_id", "type"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompanyImageType type;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    private CompanyImage(Company company, CompanyImageType type, String imageUrl) {
        this.company = company;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    public static CompanyImage create(Company company, CompanyImageType type, String imageUrl) {
        return new CompanyImage(company, type, imageUrl);
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}