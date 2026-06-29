package org.sopt.haphap.domain.alram.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.code.AlramSuccessCode;
import org.sopt.haphap.domain.alram.dto.PushTokenRegisterRequest;
import org.sopt.haphap.domain.alram.service.PushTokenRegisterService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alrams/device")
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenRegisterService pushTokenRegisterService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> register(
            @RequestHeader("X-Member-Id") Long userId,
            @Valid @RequestBody PushTokenRegisterRequest request) {

        pushTokenRegisterService.register(userId, request);
        SuccessResponse<Void> body = ApiResponse.success(AlramSuccessCode.DEVICE_TOKEN_REGISTERED);
        return ResponseEntity.status(body.status()).body(body);
    }
}