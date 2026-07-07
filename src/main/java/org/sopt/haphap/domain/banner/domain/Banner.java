package org.sopt.haphap.domain.banner.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "banner",
        indexes = @Index(name = "idx_banner_display_order", columnList = "display_order")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 200)
    private String mainMessage;

    @Column(nullable = false, length = 200)
    private String subMessage;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean isActive;

    private Banner(String imageUrl, String mainMessage, String subMessage, Integer displayOrder) {
        this.imageUrl = imageUrl;
        this.mainMessage = mainMessage;
        this.subMessage = subMessage;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }

    public static Banner create(String imageUrl, String mainMessage, String subMessage, Integer displayOrder) {
        return new Banner(imageUrl, mainMessage, subMessage, displayOrder);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}