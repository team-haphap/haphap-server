package org.sopt.haphap.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.code.CalendarErrorCode;
import org.sopt.haphap.domain.calendar.dto.CalendarDateIndicatorResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarIndicatorListResponse;
import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarIndicatorQueryService {

    private final PostingStageRepository postingStageRepository;
    private static final YearMonth MIN = YearMonth.of(2000, 1);
    private static final YearMonth MAX = YearMonth.of(2030, 12);

    public CalendarIndicatorListResponse getIndicators(YearMonth yearMonth) {
        validateRange(yearMonth);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<PostingStageCalendarProjection> stages =
                postingStageRepository.findCalendarStagesByDateRange(start, end);

        Map<LocalDate, PostingStageCalendarProjection> stageByDate = stages.stream()
                .collect(Collectors.toMap(
                        PostingStageCalendarProjection::getExpectedAnnouncementDate,
                        Function.identity(),
                        CalendarStageMerger::pickHigherScore));

        List<CalendarDateIndicatorResponse> dates = start.datesUntil(end.plusDays(1))
                .map(date -> CalendarDateIndicatorResponse.of(date, toLikelihood(stageByDate.get(date))))
                .toList();

        return CalendarIndicatorListResponse.of(dates);
    }

    private AnnouncementLikelihood toLikelihood(PostingStageCalendarProjection stage) {
        return stage == null ? AnnouncementLikelihood.NONE : AnnouncementLikelihood.from(stage.getExpectedScore());
    }

    private void validateRange(YearMonth yearMonth) {
        if (yearMonth.isBefore(MIN) || yearMonth.isAfter(MAX)) {
            throw new CustomException(CalendarErrorCode.UNSUPPORTED_DATE_RANGE);
        }
    }
}