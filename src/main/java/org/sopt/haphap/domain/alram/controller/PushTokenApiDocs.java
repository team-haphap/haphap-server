package org.sopt.haphap.domain.alram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.alram.dto.PushTokenRegisterRequest;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "파이어베이스 토큰 등록", description = "파이어베이스 토큰 등록을 위한 API ")
public interface PushTokenApiDocs {

    @Operation(summary = "FCM 디바이스 토큰 등록/갱신",
            description = """
                    기기의 토큰을 등록합니다.
                    - 디바이스 아이디, FCM토큰, 디바이스 타입을 requestbody에 넣어주세요. 
                    - 디바이스 타입은 ANDROID, IOS, WEB 이 있습니다.(확장용. 일단 ANDROID로 하면 될 것 같아요!)
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "등록 성공",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "DEVICE_TOKEN_REGISTERED",
                      "message": "디바이스 토큰이 등록되었습니다.",
                      "data": null
                    }
                    """))
    )
    ResponseEntity<SuccessResponse<Void>> register(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PushTokenRegisterRequest request);
}