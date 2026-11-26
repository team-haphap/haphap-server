package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.dto.request.RegistrationReviewResolveRequest;
import org.sopt.haphap.domain.registration.dto.response.RegistrationReviewResponse;
import org.sopt.haphap.domain.registration.service.RegistrationReviewService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/registration-reviews")
@RequiredArgsConstructor
@Hidden
public class AdminRegistrationReviewController {

    private final RegistrationReviewService registrationReviewService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<RegistrationReviewResponse>>> getPendingReviews() {
        List<RegistrationReviewResponse> response = registrationReviewService.getPendingReviews();
        return ResponseEntity.ok(ApiResponse.success(RegistrationSuccessCode.REVIEW_LIST_FETCHED, response));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<RegistrationReviewResponse>> resolve(
            @PathVariable Long reviewId, @Valid @RequestBody RegistrationReviewResolveRequest request) {
        RegistrationReviewResponse response = registrationReviewService.resolve(reviewId, request.accept());
        return ResponseEntity.ok(ApiResponse.success(RegistrationSuccessCode.REVIEW_RESOLVED, response));
    }
}
