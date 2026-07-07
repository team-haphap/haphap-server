package org.sopt.haphap.domain.banner.repository;

import java.util.List;
import org.sopt.haphap.domain.banner.domain.Banner;
import org.sopt.haphap.domain.banner.dto.response.BannerResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("""
            SELECT new org.sopt.haphap.domain.banner.dto.response.BannerResponse(b.imageUrl, b.displayOrder)
            FROM Banner b
            WHERE b.isActive = true
            ORDER BY b.displayOrder ASC
            """)
    List<BannerResponse> findActiveBannersOrderByDisplayOrder();
}