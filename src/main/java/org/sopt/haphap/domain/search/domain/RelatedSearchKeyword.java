package org.sopt.haphap.domain.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "related_search_keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelatedSearchKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(nullable = false)
    private boolean isActive;

    private RelatedSearchKeyword(String keyword) {
        this.keyword = keyword;
        this.isActive = true;
    }

    public static RelatedSearchKeyword create(String keyword) {
        return new RelatedSearchKeyword(keyword);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}