package org.sopt.haphap.domain.alram.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.code.AlramSuccessCode;
import org.sopt.haphap.domain.alram.service.AlramSettingService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/postings/{postingId}/alrams")
@RequiredArgsConstructor
public class AlramController implements AlramApiDocs {

    private final AlramSettingService alramSettingService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> setAlrams(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long postingId
    ) {
        alramSettingService.setAlrams(userId,postingId);
        SuccessResponse<Void> body = ApiResponse.success(AlramSuccessCode.ALRAM_REGISTERED);
        return ResponseEntity.status(body.status()).body(body);
    }
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteAlrams(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long postingId
    ) {

        alramSettingService.deleteAlrams(userId,postingId);
        SuccessResponse<Void> body = ApiResponse.success(AlramSuccessCode.ALRAM_DELETED);
        return ResponseEntity.status(body.status()).body(body);
    }
}
