package org.sopt.haphap.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.dto.CalendarDateIndicatorResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarIndicatorListResponse;
import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarIndicatorQueryService {

    private final PostingStageRepository postingStageRepository;

    public CalendarIndicatorListResponse getIndicators(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // 그 달 전체 stage를 쿼리 1번으로 처리
        List<PostingStageCalendarProjection> stages =
                postingStageRepository.findCalendarStagesByDateRange(start, end);

        // 날짜별 최고 점수만 메모리에서 집계 (여러 공고 있으면 가장 높은 값 기준)
        Map<LocalDate, Integer> maxScoreByDate = stages.stream()
                .collect(Collectors.toMap(
                        PostingStageCalendarProjection::getExpectedAnnouncementDate,
                        PostingStageCalendarProjection::getExpectedScore,
                        Math::max));

        List<CalendarDateIndicatorResponse> dates = start.datesUntil(end.plusDays(1))
                .map(date -> CalendarDateIndicatorResponse.of(date, toLikelihood(maxScoreByDate.get(date))))
                .toList();

        return CalendarIndicatorListResponse.of(dates);
    }

    private AnnouncementLikelihood toLikelihood(Integer maxScore) {
        return maxScore == null ? AnnouncementLikelihood.NONE : AnnouncementLikelihood.from(maxScore);
    }
}