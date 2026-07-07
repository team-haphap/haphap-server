package org.sopt.haphap.domain.banner.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.banner.code.BannerSuccessCode;
import org.sopt.haphap.domain.banner.dto.response.BannerListResponse;
import org.sopt.haphap.domain.banner.service.BannerService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/banners")
public class BannerController implements BannerApiDocs {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<SuccessResponse<BannerListResponse>> getBanners() {
        BannerListResponse response = bannerService.getBanners();

        SuccessResponse<BannerListResponse> body =
                ApiResponse.success(BannerSuccessCode.BANNER_LIST_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }
}