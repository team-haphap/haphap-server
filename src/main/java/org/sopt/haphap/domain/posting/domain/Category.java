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
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String cardImageUrl;

    private Category(String name) {
        this.name = name;
    }

    public static Category create(String name) {
        return new Category(name,null);
    }

    private Category(String name, String cardImageUrl) {
        this.name = name;
        this.cardImageUrl = cardImageUrl;
    }

    public static Category create(String name, String cardImageUrl) {
        return new Category(name, cardImageUrl);
    }
}
