package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.registration.dto.response.VerificationSummaryResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "합격 인증")
public interface MemberVerificationApiDocs {

    @Operation(
            summary = "합격 인증 현황 조회 (승인/반려)",
            description = """
                본인의 합격 인증 요청을 최신순으로 반환합니다.
                인증 대상(PASS)이 아닌 등록은 포함하지 않습니다.
                companyLogoImageUrl은 홈/캘린더와 같은 정사각형 CALENDAR_LOGO입니다.
                반려(REJECTED) 항목의 submittedImageUrls는 5분 유효한 비공개 이미지 URL입니다.
                반려(REJECTED) 항목의 rejectReason은 관리자가 입력한 반려 사유입니다.
                재제출 후에는 새 제출 이미지로 갱신되고 이전 반려 사유는 초기화됩니다.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "status": 200,
                  "code": "VERIFICATION_SUMMARY_FETCHED",
                  "message": "합격 인증 현황 조회에 성공했습니다.",
                  "data": {
                    "pendingCount": 0,
                    "approvedCount": 0,
                    "rejectedCount": 1,
                    "items": [
                      {
                        "registrationId": 501,
                        "companyName": "카카오",
                        "companyLogoImageUrl": "https://.../calendar-kakao.png",
                        "postingTitle": "2026 신입 공개 채용",
                        "stageName": "서류",
                        "status": "REJECTED",
                        "submittedAt": "2026-11-12T10:00:00",
                        "resolvedAt": "2026-11-12T10:30:00",
                        "resubmittable": true,
                        "submittedImageUrls": [
                          "https://...presigned-image-url..."
                        ],
                        "rejectReason": "합격을 인증할 수 있는 화면을 다시 제출해주세요."
                      }
                    ]
                  }
                }
                """))
    )
    ResponseEntity<SuccessResponse<VerificationSummaryResponse>> getSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}