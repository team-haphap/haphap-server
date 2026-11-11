package org.sopt.haphap.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.scrap.code.ScrapSuccessCode;
import org.sopt.haphap.domain.scrap.dto.response.ScrapResponse;
import org.sopt.haphap.domain.scrap.service.ScrapService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scraps")
@RequiredArgsConstructor
public class ScrapController implements ScrapApiDocs {

    private final ScrapService scrapService;

    @PostMapping("/{postingId}")
    public ResponseEntity<SuccessResponse<ScrapResponse>> scrap(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId) {
        boolean created = scrapService.scrap(userId, postingId);
        ScrapSuccessCode code = created ? ScrapSuccessCode.SCRAP_CREATED : ScrapSuccessCode.ALREADY_SCRAPPED;
        SuccessResponse<ScrapResponse> body = ApiResponse.success(code, ScrapResponse.scrapped(postingId));
        return ResponseEntity.status(body.status()).body(body);
    }

    @DeleteMapping("/{postingId}")
    public ResponseEntity<SuccessResponse<ScrapResponse>> unscrap(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId) {
        boolean deleted = scrapService.unscrap(userId, postingId);
        ScrapSuccessCode code = deleted ? ScrapSuccessCode.SCRAP_DELETED : ScrapSuccessCode.ALREADY_UNSCRAPPED;
        SuccessResponse<ScrapResponse> body = ApiResponse.success(code, ScrapResponse.unscrapped(postingId));
        return ResponseEntity.status(body.status()).body(body);
    }
}
