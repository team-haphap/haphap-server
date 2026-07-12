package org.sopt.haphap.domain.banner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.banner.dto.response.BannerListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "배너", description = "홈 화면 히어로 배너 관련 API 입니다")
public interface BannerApiDocs {

    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BannerListResponse.class),
                    examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "BANNER_LIST_FETCHED",
                      "message": "히어로 배너 목록 조회에 성공했습니다.",
                      "data": {
                        "banners": [
                          { "imageUrl": "https://.../banner1.png", "displayOrder": 1 },
                          { "imageUrl": "https://.../banner2.png", "displayOrder": 2 }
                        ]
                      }
                    }
                    """)))
    @Operation(summary = "홈 히어로 배너 목록 조회",
            description = """
                    홈 화면 진입 시 노출할 히어로 배너 이미지 목록을 노출 순서(displayOrder) 오름차순으로 반환합니다.
                    - 비활성(isActive=false) 배너는 응답에서 제외됩니다.
                    - 메인/서브 메시지는 전역 고정 문구로 클라이언트에서 관리하며 응답에 포함하지 않습니다.
                    - 배너 자동 전환(5초 간격)·스와이프 전환·인디케이터 갱신은 클라이언트가 이 응답을 기준으로 로컬에서 처리합니다.
                    """
    )
    ResponseEntity<SuccessResponse<BannerListResponse>> getBanners();
}