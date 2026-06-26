package org.sopt.haphap.registration.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.registration.dto.RegistrationCreateRequest;
import org.sopt.haphap.registration.dto.RegistrationCreateResponse;
import org.sopt.haphap.registration.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody RegistrationCreateRequest request
    ) {
        RegistrationCreateResponse response = registrationService.createRegistration(memberId, request);

        SuccessResponse<RegistrationCreateResponse> body =
                ApiResponse.success(RegistrationSuccessCode.REGISTRATION_CREATED, response);

        return ResponseEntity.status(body.status()).body(body);
    }
}