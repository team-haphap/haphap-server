package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoImageUrl;

    private String imageUrl;

    private String cardLogoImageUrl; //합격카드 로고이미지용

    private Company(String name, String description,String logoImageUrl, String imageUrl,String cardLogoImageUrl) {
        this.name = name;
        this.description = description;
        this.logoImageUrl = logoImageUrl;
        this.imageUrl = imageUrl;
        this.cardLogoImageUrl = cardLogoImageUrl;
    }

    public static Company create(String name, String description,String logoImageUrl, String imageUrl,String cardLogoImageUrl) {
        return new Company(name, description, logoImageUrl, imageUrl,cardLogoImageUrl);
    }

    private Company(String name) {
        this.name = name;
    }

    public static Company create(String name) {
        return new Company(name);
    }
}
