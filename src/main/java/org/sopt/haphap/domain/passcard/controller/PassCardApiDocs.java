package org.sopt.haphap.domain.passcard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveListResponse;
import org.sopt.haphap.domain.passcard.dto.response.UnconfirmedPassCardListResponse;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "합격카드", description = "합격카드 노출/보관함/확인 처리 API 입니다")
public interface PassCardApiDocs {

    @Operation(summary = "미확인 합격카드 조회",
            description = """
                앱 실행(스플래시) 시 홈 진입 전에 호출합니다.
                - 승인(APPROVED)됐지만 아직 확인하지 않은 본인의 합격카드를 승인 순서(오래된 것부터)로 반환합니다.
                - 빈 배열이면 홈으로 진입하고, 1건 이상이면 홈보다 우선하여 합격카드 화면을 노출합니다.
                - 알림 활성/비활성 사용자 모두 동일하게 사용합니다.
                """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })

    ResponseEntity<SuccessResponse<UnconfirmedPassCardListResponse>> getUnconfirmedPassCards(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);


    @Operation(summary = "합격카드 상세 조회",
            description = """
                    푸시 알림 탭, 보관함 [다시 보기]에서 사용합니다.
                    조회만으로는 확인 완료 상태가 바뀌지 않습니다(확인 완료는 POST /{registrationId}/confirm).
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PassCardResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "PASS_CARD_FETCHED",
                                      "message": "합격카드 조회에 성공했습니다.",
                                      "data": {
                                        "registrationId": 501,
                                        "userName": "박연수",
                                        "companyName": "카카오",
                                        "companyCardLogoImageUrl": "https://.../kakao-card.png",
                                        "title": "카카오 2026 신입 카카오 AI 신규 서비스 기획 공개 채용",
                                        "cardImageUrl": "https://.../card-planning.png",
                                        "confirmed": false
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "등록이 없거나 본인 것이 아니거나 아직 승인되지 않음",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "PASS_CARD_NOT_FOUND", "message": "합격카드를 찾을 수 없습니다." }
                                    """)))
    })
    ResponseEntity<SuccessResponse<PassCardResponse>> getPassCard(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "등록 ID") Long registrationId);

    @Operation(summary = "합격카드 확인 완료 처리",
            description = """
                    합격카드 화면이 사용자에게 정상 노출된 시점에 호출합니다. 멱등이며 본문 없이 204를 반환합니다.
                    앱 실행/푸시 수신만으로는 확인 완료가 아니며, [저장하기] 여부와는 별개입니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공 (이미 확인한 경우 포함)"),
            @ApiResponse(responseCode = "404", description = "등록이 없거나 본인 것이 아니거나 아직 승인되지 않음",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<Void> confirm(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "등록 ID") Long registrationId);

    @Operation(summary = "합격카드 보관함 목록 조회",
            description = "확인 완료한 합격카드를 확인 일시 최신순으로 반환합니다. 없으면 빈 배열입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<SuccessResponse<PassCardArchiveListResponse>> getArchive(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable);
}