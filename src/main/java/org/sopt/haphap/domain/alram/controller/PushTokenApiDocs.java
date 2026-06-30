package org.sopt.haphap.domain.alram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.alram.dto.PushTokenRegisterRequest;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "파이어베이스 토큰 등록",description = "파이어베이스 토큰 등록을 위한 API ")
public interface PushTokenApiDocs {
    @Operation(summary = "토큰 등록",
            description = """
                    기기의 토큰을 등록합니다. .
                    - 디바이스 아이디, FCM토큰, 디바이스 타입을 requestbody에 넣어주세요. 
                    - 디바이스 타입은 ANDROID, IOS, WEB 이 있습니다.(확장용. 일단 ANDROID로 하면 될 것 같아요!)
                    """)
    ResponseEntity<SuccessResponse<Void>> register(@RequestHeader("X-User-Id") Long userId,
                                                   @Valid @RequestBody PushTokenRegisterRequest request);

}