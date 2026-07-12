package org.sopt.haphap.domain.alram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "알람 등록", description = "공고별 알람을 on/off 합니다.")
public interface AlramApiDocs {

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알람 등록 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "ALRAM_REGISTERED",
                              "message": "알람이 설정되었습니다.",
                              "data": null
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
                            """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            )
    })
    @Operation(summary = "알림 받기 등록",
            description = """
                    알람을 등록합니다. 
                    - 공고 아이디를 받아 해당 공고의 알람 설정을 on합니다. 
                    """)
    ResponseEntity<SuccessResponse<Void>> setAlrams(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId
    );

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알람 취소 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "ALRAM_DELETED",
                              "message": "알람이 취소되었습니다.",
                              "data": null
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
                            """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            )
    })
    @Operation(summary = "알람 받기 취소",
            description = """
                    알람을 삭제합니다. 
                    - 공고 아이디를 받아 해당 공고의 알람 설정을 off합니다. 
                    """)
    ResponseEntity<SuccessResponse<Void>> deleteAlrams(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId
    );
}