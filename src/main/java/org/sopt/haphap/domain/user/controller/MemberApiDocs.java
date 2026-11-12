package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.user.dto.*;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원", description = "마이페이지 등 회원 정보 조회를 위한 API")
public interface MemberApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MEMBER_INFO_FETCHED",
                                      "message": "사용자 정보 조회에 성공했습니다.",
                                      "data": {
                                        "name": "김소프트",
                                        "anonymousName": "익명의 판다",
                                        "pointBalance":300,
                                        "email": "user@example.com",
                                        "profileImageUrl": "https://.../profile.png",
                                        "provider": "KAKAO"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """)))
    })
    @Operation(summary = "사용자 정보 조회",
            description = """
                    마이페이지에 필요한 사용자 정보를 조회합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요. (로그인한 본인 정보만 조회됩니다)
                    """)
    ResponseEntity<SuccessResponse<MemberResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberAccountResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MEMBER_ACCOUNT_INFO_FETCHED",
                                      "message": "사용자 계정 정보 조회에 성공했습니다.",
                                      "data": {
                                        "name": "박연수",
                                        "email": "yeonsoo1234@naver.com",
                                        "provider": "KAKAO"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "USER_NOT_FOUND", "message": "존재하지 않는 유저입니다." }
                                    """)))
    })
    @Operation(summary = "계정 정보 조회",
            description = """
                    계정 정보 화면에 필요한 실제 이름, 이메일, 로그인 수단을 조회합니다.
                    - name: 소셜 로그인으로 받은 실제 이름 (닉네임/anonymousName 아님)
                    - email: 소셜 로그인으로 받은 이메일
                    - provider: 로그인 수단 (KAKAO / APPLE)
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요. (로그인한 본인 정보만 조회됩니다)
                    """)
    ResponseEntity<SuccessResponse<MemberAccountResponse>> getMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MemberProfileResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MEMBER_PROFILE_UPDATED",
                                      "message": "프로필이 수정되었습니다.",
                                      "data": {
                                        "name": "박연수",
                                        "nickname": "김합합",
                                        "anonymousName": "익명의 김합합",
                                        "email": "user@example.com",
                                        "profileImageUrl": "https://.../profile-images/xxx.webp",
                                        "provider": "KAKAO"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "닉네임 형식 오류 / 이미지 파일 오류",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = {
                                    @ExampleObject(name = "허용하지 않는 문자", value = """
                                            { "status": 400, "code": "INVALID_NICKNAME_CHARACTER", "message": "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다." }
                                            """),
                                    @ExampleObject(name = "길이 오류 (2~5자)", value = """
                                            { "status": 400, "code": "INVALID_NICKNAME_LENGTH", "message": "닉네임은 2~5자로 입력해주세요." }
                                            """),
                                    @ExampleObject(name = "이미지가 아니거나 읽을 수 없음", value = """
                                            { "status": 400, "code": "INVALID_IMAGE_FILE", "message": "이미지 파일을 읽을 수 없습니다. JPG 또는 PNG 파일을 올려주세요." }
                                            """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "409", description = "다른 사용자가 사용중인 닉네임",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 409, "code": "DUPLICATE_NICKNAME", "message": "다른 사용자가 사용중인 닉네임입니다." }
                                    """))),
            @ApiResponse(responseCode = "503", description = "이미지 업로드 실패 (S3 오류 등, 재시도 가능)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 503, "code": "IMAGE_UPLOAD_FAILED", "message": "이미지 업로드에 실패했습니다. 잠시 후 다시 시도해주세요." }
                                    """)))
    })
    @Operation(summary = "프로필 수정",
            description = """
                    닉네임과 프로필 이미지를 수정합니다. multipart/form-data로 보내주세요.
                    - 두 항목 모두 선택입니다. 보낸 항목만 바뀌고, 아무것도 보내지 않으면 현재 정보를 그대로 반환합니다.
                    - nickname: "익명의 "를 뺀 부분입니다. 저장 후 anonymousName은 "익명의 " + nickname이 됩니다.
                      - 완성형 한글·영문·숫자만 가능 (공백, 특수문자, 자음/모음 단독, 이모지 불가) → INVALID_NICKNAME_CHARACTER
                      - 2~5자 (한글·영문·숫자 모두 1자로 계산) → INVALID_NICKNAME_LENGTH
                      - 허용하지 않는 문자와 길이 오류가 함께 있으면 INVALID_NICKNAME_CHARACTER가 우선입니다.
                      - 현재 내 닉네임과 같으면 중복이 아닙니다.
                    - profileImage: 이미지 파일 (최대 10MB). webp로 변환해 저장합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    ResponseEntity<SuccessResponse<MemberProfileResponse>> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "새 닉네임 (\"익명의 \" 제외, 한글·영문·숫자 2~5자)", example = "김합합") String nickname,
            @Parameter(description = "새 프로필 이미지 파일") MultipartFile profileImage);

    @Operation(
            summary = "닉네임 사용 가능 여부 확인",
            description = """
                입력한 닉네임의 형식과 중복 여부를 확인합니다.
                현재 본인의 닉네임을 다시 입력한 경우 사용 가능으로 응답합니다.
                Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용 가능",
                    content = @Content(
                            schema = @Schema(implementation = NicknameValidationResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 200,
                                  "code": "NICKNAME_AVAILABLE",
                                  "message": "사용 가능한 닉네임입니다.",
                                  "data": { "anonymousName": "익명의 김합합" }
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "닉네임 형식 오류 또는 파라미터 누락",
                    content = @Content(
                            schema = @Schema(implementation = FailureResponse.class),
                            examples = {
                                    @ExampleObject(name = "닉네임 형식 오류", value = """
                                        {
                                          "status": 400,
                                          "code": "INVALID_NICKNAME_FORMAT",
                                          "message": "한글, 영문, 숫자만 사용할 수 있어요."
                                        }
                                        """),
                                    @ExampleObject(name = "nickname 파라미터 누락", value = """
                                        {
                                          "status": 400,
                                          "code": "INVALID_INPUT_VALUE",
                                          "message": "유효하지 않은 입력값입니다."
                                        }
                                        """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 401,
                                  "code": "UNAUTHORIZED",
                                  "message": "인증이 필요합니다."
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "code": "USER_NOT_FOUND",
                                  "message": "존재하지 않는 유저입니다."
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "다른 사용자가 이미 사용 중인 닉네임",
                    content = @Content(
                            schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 409,
                                  "code": "NICKNAME_DUPLICATED",
                                  "message": "다른 사용자가 사용중인 닉네임이에요."
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 500,
                                  "code": "INTERNAL_SERVER_ERROR",
                                  "message": "서버 내부 오류가 발생했습니다."
                                }
                                """)
                    )
            )
    })
    ResponseEntity<SuccessResponse<NicknameValidationResponse>> validateNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "한글·영문·숫자 2~5자", example = "김합합")
            @RequestParam("nickname") String nickname);

    @Operation(summary = "회원 탈퇴",
            description = """
                    본인 계정을 즉시 탈퇴 처리합니다. 
                    (1) 요청 즉시: 개인정보 파기, 알림·알림 설정·푸시 토큰·조회 이력·찜한 공고 삭제, 합불 기록 비식별화(전체 집계는 유지),
                        합격 인증 이미지 삭제(약 25시간 이내), 액세스/리프레시 토큰 폐기
                    (2) 카카오/애플 연동 해제는 응답 이후 진행되며, 실패 시 서버가 자동 재시도합니다. 연동 해제 결과와 무관하게 204를 반환합니다.
                    (3) 탈퇴 사유 필수. ETC 선택 시 etcReason 필요 (앞쪽 공백 제외 1~150자, 이모지 1개 = 1자, 공백만 입력 불가)
                    (4) 동시에 들어온 중복 요청도 204를 반환합니다.
                        단, 탈퇴 완료 후 같은 토큰으로 재요청하면 401이 반환되니 '탈퇴 완료'로 간주하고 로그인 화면으로 이동해주세요.
                    (5) 처리 중 서버 오류(5xx)가 나면 어떤 데이터도 변경되지 않습니다. 재시도해주세요.
                    (6) 탈퇴 직후 연동 해제가 끝나기 전에 같은 소셜 계정으로 로그인하면 403 WITHDRAWAL_IN_PROGRESS가 반환됩니다.
                    (7) Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = WithdrawRequest.class),
                    examples = {
                            @ExampleObject(name = "일반 사유", value = """
                                    { "reason": "LOW_USAGE" }
                                    """),
                            @ExampleObject(name = "기타 사유", value = """
                                    { "reason": "ETC", "etcReason": "알림이 너무 많이 와요" }
                                    """)
                    }))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공 / 이미 처리된 중복 요청 (응답 본문 없음)"),
            @ApiResponse(responseCode = "400", description = "탈퇴 사유 누락 / 기타 사유 공백 / 150자 초과",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 400, "code": "INVALID_INPUT_VALUE", "message": "기타 사유를 150자 이하로 입력해주세요." }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음, 또는 이미 탈퇴 완료된 토큰)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 이미 탈퇴한 회원",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "USER_NOT_FOUND", "message": "존재하지 않는 유저입니다." }
                                    """)))
    })
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,  // ← hidden 추가
            @Valid @RequestBody WithdrawRequest request);
}