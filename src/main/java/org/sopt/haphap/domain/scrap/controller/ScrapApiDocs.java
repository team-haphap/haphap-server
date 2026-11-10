package org.sopt.haphap.domain.scrap.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.scrap.dto.response.ScrapResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "찜하기", description = "공고 찜하기 관련 API 입니다")
public interface ScrapApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "새로 찜함",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "SCRAP_CREATED",
                                      "message": "공고를 찜했습니다.",
                                      "data": { "postingId": 54, "isScrapped": true }
                                    }
                                    """))),
            @ApiResponse(responseCode = "200", description = "이미 찜한 공고 (변경 없음)",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "ALREADY_SCRAPPED",
                                      "message": "이미 찜한 공고입니다.",
                                      "data": { "postingId": 54, "isScrapped": true }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "POSTING_NOT_FOUND", "message": "존재하지 않는 공고입니다." }
                                    """)))
    })
    @Operation(summary = "공고 찜하기",
            description = """
                    공고를 찜합니다. 마감된 공고도 찜할 수 있습니다.
                    - 멱등: 이미 찜한 공고에 다시 요청해도 에러 없이 200(ALREADY_SCRAPPED)을 반환합니다.
                      버튼 연타/재시도로 동시에 여러 번 요청돼도 찜은 한 건만 저장됩니다.
                    - 새로 찜하면 201(SCRAP_CREATED). 두 경우 모두 data.isScrapped는 true입니다.
                    """)
    ResponseEntity<SuccessResponse<ScrapResponse>> scrap(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "찜할 공고 ID") Long postingId);
}
