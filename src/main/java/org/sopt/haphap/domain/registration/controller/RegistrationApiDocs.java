package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.registration.dto.request.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.response.RegistrationCreateResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "상태 등록",description = "사용자의 합/불/대기 상태 등록을 위한 API ")
public interface RegistrationApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = RegistrationCreateResponse.class),
                            examples = {
                                    @ExampleObject(name = "PASS(합격) - 카드 정보 포함", value = """
                                    {
                                      "status": 201,
                                      "code": "REGISTRATION_CREATED",
                                      "message": "상태 등록이 완료되었습니다.",
                                      "data": {
                                        "registrationId": 101,
                                        "card": {
                                          "userName": "김소프트",
                                          "companyName": "토스",
                                          "companyCardLogoImageUrl": "https://.../toss_logo.png",
                                          "title": "2026 상반기 신입 공채",
                                          "stageName": "최종 면접",
                                          "categoryName": "개발",
                                          "cardImageUrl": "https://.../pass_card_bg.png"
                                        }
                                      }
                                    }
                                    """),
                                    @ExampleObject(name = "FAIL/PENDING - 카드 없음", value = """
                                    {
                                      "status": 201,
                                      "code": "REGISTRATION_CREATED",
                                      "message": "상태 등록이 완료되었습니다.",
                                      "data": {
                                        "registrationId": 102,
                                        "card": null
                                      }
                                    }
                                    """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                - PENDING_MUST_NOT_HAVE_CONTACT : 대기 상태에서는 연락 정보를 보낼 수 없습니다.
                - CONFIRMED_MUST_HAVE_CONTACT : 합격/불합격 결과에는 연락 정보가 필요합니다.
                - INVALID_CONTACT_METHOD: 유효하지 않은 연락 수단입니다.)
                - 요청값 검증 실패 (필수 필드 누락)
                """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = {
                                    @ExampleObject(name = "CONFIRMED_MUST_HAVE_CONTACT", value = """
                                    { "status": 400, "code": "CONFIRMED_MUST_HAVE_CONTACT", "message": "합격/불합격 결과에는 연락 정보가 필요합니다." }
                                    """),
                                    @ExampleObject(name = "요청값 검증 실패 (필수 필드 누락)", value = """
                                    { "status": 400, "code": "INVALID_INPUT_VALUE", "message": "공고 ID는 필수입니다." }
                                    """),
                                    @ExampleObject(name = "INVALID_CONTACT_METHOD", value = """
                                    { "status": 400, "code": "INVALID_CONTACT_METHOD", "message": "유효하지 않은 연락 수단입니다." }
                                    """)
                            })
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                - DUPLICATE_REGISTRATION : 이미 입력한 전형입니다.
                """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            )
    })
    @Operation(summary = "합불 현황 등록" ,
            description = """
                공고 . 전형 별 사용자의 상태를 등록하고 알람 여부를 설정합니다.
                - PENDING 상태 인 경우 contactMethod와 contactedDate/contactedTime 필드를 null로 해주세요
                - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                - EMAIL(이메일),SMS(문자),PAGE(기업 홈페이지),PHONE_CALL(전화)
                """)
    ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegistrationCreateRequest request);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검증 성공",
                    content = @Content(examples = {
                            @ExampleObject(name = "신규 등록 (등록 이력 없음)", value = """
                                    {
                                      "status": 200,
                                      "code": "NEW_REGISTRATION",
                                      "message": "해당 전형의 새로운 상태입니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "기존 PENDING 존재 (변경 확인 필요)", value = """
                                    {
                                      "status": 200,
                                      "code": "REGISTRATION_CONFIRM_REQUIRED",
                                      "message": "전형 사항을 변경여부 토글을 띄워주세요.",
                                      "data": null
                                    }
                                    """)
                    })),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
                - STAGE_NOT_FOUND : 존재하지 않는 전형입니다.
                """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                - STAGE_NOT_IN_POSTING : 해당 공고의 전형 단계가 아닙니다.
                """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                - DUPLICATE_REGISTRATION : 이미 입력한 전형입니다.
                """,
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            )
    })
    @Operation(summary = "해당 공고 동일 전형 등록 여부 조회(등록 유효성 검증)" ,
            description = """
                해당 공고의 동일 전형에 이미 등록하였는지 여부를 검증합니다. .
                - '아직 몰라요' 외의 다른 데이터가 있는 경우 409 에러로 처리합니다. 
                - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                """)
    ResponseEntity<SuccessResponse<Void>> check(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId,
            @PathVariable Long stageId);
}