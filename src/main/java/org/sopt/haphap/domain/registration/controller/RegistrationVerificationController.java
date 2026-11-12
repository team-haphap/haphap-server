package org.sopt.haphap.domain.registration.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.dto.request.VerificationResubmitRequest;
import org.sopt.haphap.domain.registration.dto.response.VerificationResubmitResponse;
import org.sopt.haphap.domain.registration.service.RegistrationVerificationService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationVerificationController implements RegistrationVerificationApiDocs {

    private final RegistrationVerificationService registrationVerificationService;

    @PutMapping("/{registrationId}/verification-images")
    public ResponseEntity<SuccessResponse<VerificationResubmitResponse>> resubmit(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long registrationId,
            @Valid @RequestBody VerificationResubmitRequest request) {
        SuccessResponse<VerificationResubmitResponse> body = ApiResponse.success(
                RegistrationSuccessCode.VERIFICATION_RESUBMITTED,
                registrationVerificationService.resubmit(userId, registrationId, request.verificationImageIds()));
        return ResponseEntity.status(body.status()).body(body);
    }
}