package org.sopt.haphap.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.calendar.dto.CalendarIndicatorListResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "캘린더", description = "캘린더 관련 API 입니다")
public interface CalendarApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarPostingListResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "CALENDAR_POSTINGS_FETCHED",
                              "message": "날짜별 공고 카드 조회에 성공했습니다.",
                              "data": {
                                "date": "2026-07-15",
                                "postings": [
                                  {
                                    "postingId": 12,
                                    "title": "토스 2026 상반기 신입 공채",
                                    "stageName": "1차 서류",
                                    "likelihood": "HIGH",
                                    "participantCount": 34,
                                    "logoImageUrl": "https://.../logo.png"
                                  }
                                ]
                              }
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 날짜 범위 (2000-01-01~2030-12-31 이외)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    @Operation(summary = "날짜별 공고 카드 리스트 조회",
            description = """
                    선택한 날짜에 발표 예정인 공고 카드 목록을 반환합니다.
                정렬 순서 : 발표 예상 점수 내림차순 (동점 시 공고명 가나다순)
                대표 전형 : 한 공고에 같은 날짜로 겹치는 전형이 여러 개면, 실제 등록 진행 상황(확정 등록 15명 도달 시 다음 전형으로 진행) 기준 "현재 전형"을 대표로 표시합니다. 판단 불가 시 발표 예상 점수가 높은 전형으로 대체합니다.
                참여 인원(participantCount) : 해당 공고의 모든 전형에서 상태를 등록한 인원 수입니다. (중복 유저 제거, 전형 구분 없이 합산)
                해당 날짜에 발표 예정 공고가 없으면 빈 배열을 반환합니다.
                - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                """)
    ResponseEntity<SuccessResponse<CalendarPostingListResponse>> getPostingsByDate(
            @RequestParam String date
    );

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarIndicatorListResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "CALENDAR_INDICATOR_FETCHED",
                              "message": "월별 캘린더 인디케이터 조회에 성공했습니다.",
                              "data": {
                                "dates": [
                                  { "date": "2026-07-01", "likelihood": "NONE" },
                                  { "date": "2026-07-15", "likelihood": "HIGH" },
                                  { "date": "2026-07-31", "likelihood": "VERY_HIGH" }
                                ]
                              }
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 연월 범위 (2000-01~2030-12 이외)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    @Operation(summary = "월별 캘린더 인디케이터 조회",
            description = """
                   특정 연월의 모든 날짜에 대해 발표 가능성 인디케이터 등급(NONE/VERY_LOW/LOW/MEDIUM/HIGH/VERY_HIGH)을 반환합니다.
                    동일 날짜에 공고가 여러 개면 가장 높은 등급을 그 날짜의 등급으로 표시합니다.
                    지원 범위: 2000-01 ~ 2030-12 (범위 밖 요청은 400 에러)
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    ResponseEntity<SuccessResponse<CalendarIndicatorListResponse>> getIndicators(
            @RequestParam String date
    );
}