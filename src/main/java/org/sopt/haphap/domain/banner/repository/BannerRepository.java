package org.sopt.haphap.domain.banner.repository;

import java.util.List;
import org.sopt.haphap.domain.banner.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();
}