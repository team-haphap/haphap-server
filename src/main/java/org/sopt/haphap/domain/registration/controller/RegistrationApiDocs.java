package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "상태 등록",description = "사용자의 합/불/대기 상태 등록을 위한 API ")
public interface RegistrationApiDocs {

    @Operation(summary = "상태 등록" ,
            description = """
                공고 . 전형 별 사용자의 상태를 등록하고 알람 여부를 설정합니다.
                - force값은 필수 값이 아닙니다. 
                - 기존 공고,전형에 '아직 몰라요'에 대한 데이터가 있고, 사용자가 업데이트를 원할 경우 force= true 로 설정합니다.
                - '아직 몰라요' 외의 다른 데이터가 있는 경우 409 에러로 처리합니다. 
                """)
    ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(@RequestHeader("X-User-Id") Long userId,
                                                                                   @Valid @RequestBody RegistrationCreateRequest request);
}
