package org.sopt.haphap.domain.registration.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateResponse;
import org.sopt.haphap.domain.registration.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/registrations")
public class RegistrationController implements RegistrationApiDocs {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RegistrationCreateRequest request
    ) {
        RegistrationCreateResponse response = registrationService.createRegistration(userId, request);

        RegistrationSuccessCode code = switch (response.status()) {
            case CREATED -> RegistrationSuccessCode.REGISTRATION_CREATED;
            case UPDATED -> RegistrationSuccessCode.REGISTRATION_UPDATED;
            case CONFIRM_REQUIRED -> RegistrationSuccessCode.REGISTRATION_CONFIRM_REQUIRED;
        };

        SuccessResponse<RegistrationCreateResponse> body =
                ApiResponse.success(code, response);

        return ResponseEntity.status(body.status()).body(body);
    }
}