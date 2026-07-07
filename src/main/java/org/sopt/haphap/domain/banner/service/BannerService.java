package org.sopt.haphap.domain.banner.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.banner.dto.response.BannerListResponse;
import org.sopt.haphap.domain.banner.dto.response.BannerResponse;
import org.sopt.haphap.domain.banner.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerListResponse getBanners() {
        List<BannerResponse> banners = bannerRepository.findActiveBannersOrderByDisplayOrder();
        return BannerListResponse.from(banners);
    }
}