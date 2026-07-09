package org.sopt.haphap.domain.registration.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.service.RegistrationCheckService;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.dto.request.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.response.RegistrationCreateResponse;
import org.sopt.haphap.domain.registration.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/registrations")
public class RegistrationController implements RegistrationApiDocs {

    private final RegistrationService registrationService;
    private final RegistrationCheckService registrationCheckService;

    @PostMapping
    public ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegistrationCreateRequest request
    ) {
        RegistrationCreateResponse response = registrationService.createRegistration(userId, request);
        SuccessResponse<RegistrationCreateResponse> body =
                ApiResponse.success(RegistrationSuccessCode.REGISTRATION_CREATED, response);
        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{postingId}/{stageId}")
    public ResponseEntity<SuccessResponse<Void>> check(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId,
            @PathVariable Long stageId) {

        SuccessResultCode code = registrationCheckService.check(userId, postingId, stageId);
        SuccessResponse<Void> body = ApiResponse.success(code);
        return ResponseEntity.status(body.status()).body(body);
    }
}