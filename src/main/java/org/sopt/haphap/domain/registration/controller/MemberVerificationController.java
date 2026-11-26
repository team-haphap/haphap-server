package org.sopt.haphap.domain.registration.controller;


import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.dto.response.VerificationSummaryResponse;
import org.sopt.haphap.domain.registration.service.VerificationQueryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/verifications")
@RequiredArgsConstructor
public class MemberVerificationController implements MemberVerificationApiDocs {

    private final VerificationQueryService verificationQueryService;

    @GetMapping("/summary")
    public ResponseEntity<SuccessResponse<VerificationSummaryResponse>> getSummary(
            @AuthenticationPrincipal Long userId) {
        SuccessResponse<VerificationSummaryResponse> body = ApiResponse.success(
                RegistrationSuccessCode.VERIFICATION_SUMMARY_FETCHED,
                verificationQueryService.getSummary(userId));
        return ResponseEntity.status(body.status()).body(body);
    }
}