package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "회원", description = "마이페이지 등 회원 정보 조회를 위한 API")
public interface MemberApiDocs {

    @Operation(summary = "사용자 정보 조회",
            description = """
                    마이페이지에 필요한 사용자 정보를 조회합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    ResponseEntity<SuccessResponse<MemberResponse>> getMyInfo(@RequestHeader("Authorization") String authorization);
}