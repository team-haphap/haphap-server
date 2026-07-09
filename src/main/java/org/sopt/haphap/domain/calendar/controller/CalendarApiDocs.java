package org.sopt.haphap.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.calendar.dto.CalendarIndicatorListResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "캘린더", description = "캘린더 관련 API 입니다")
public interface CalendarApiDocs {

    @Operation(summary = "날짜별 공고 카드 리스트 조회",
            description = """
                    선택한 날짜에 발표 예정인 공고 카드 목록을 반환합니다.
                    정렬 순서 : 발표 예상 점수 내림차순 (동점 시 공고명 가나다순)
                    해당 날짜에 발표 예정 공고가 없으면 빈 배열을 반환합니다.
                    지원 범위: 2000-01-01 ~ 2030-12-31 (범위 밖 요청은 400 에러)
                    """)
    @ApiResponse(responseCode = "400", description = "지원하지 않는 날짜 범위 (2000-01-01~2030-12-31 이외)",
            content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    ResponseEntity<SuccessResponse<CalendarPostingListResponse>> getPostingsByDate(
            @RequestParam String date
    );
    @Operation(summary = "월별 캘린더 인디케이터 조회",
            description = """
                   특정 연월의 모든 날짜에 대해 발표 가능성 인디케이터 등급(NONE/VERY_LOW/LOW/MEDIUM/HIGH/VERY_HIGH)을 반환합니다.
                    동일 날짜에 공고가 여러 개면 가장 높은 등급을 그 날짜의 등급으로 표시합니다.
                    지원 범위: 2000-01 ~ 2030-12 (범위 밖 요청은 400 에러)
                    """)
    @ApiResponse(responseCode = "400", description = "지원하지 않는 연월 범위 (2000-01~2030-12 이외)",
            content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    ResponseEntity<SuccessResponse<CalendarIndicatorListResponse>> getIndicators(
            @RequestParam String date
    );
}