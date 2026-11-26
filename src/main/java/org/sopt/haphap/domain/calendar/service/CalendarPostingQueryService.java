package org.sopt.haphap.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.code.CalendarErrorCode;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingCardResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.domain.calendar.service.support.CalendarRepresentativeStageResolver;
import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.projection.PostingParticipantCountProjection;
import org.sopt.haphap.domain.registration.projection.StagePendingCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarPostingQueryService {

    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final CalendarRepresentativeStageResolver representativeStageResolver;

    private static final YearMonth MIN = YearMonth.of(2000, 1);
    private static final YearMonth MAX = YearMonth.of(2030, 12);

    public CalendarPostingListResponse getPostingsByDate(LocalDate date) {
        validateRange(YearMonth.from(date));
        List<PostingStageCalendarProjection> stages =
                postingStageRepository.findCalendarStagesByDate(date, CompanyImageType.CALENDAR_LOGO);

        if (stages.isEmpty()) {
            return CalendarPostingListResponse.of(date, List.of());
        }

        Map<Long, PostingStageCalendarProjection> stageByPostingId = representativeStageResolver.resolve(stages);
        List<Long> postingIds = List.copyOf(stageByPostingId.keySet());

        // 참여중 인원 = 해당 공고의 모든 전형에서 상태등록한 유저 수 (중복 제거)
        Map<Long, Long> participantCountByPostingId = registrationRepository
                .countDistinctUsersByPostingIds(postingIds).stream()
                .collect(Collectors.toMap(
                        PostingParticipantCountProjection::getPostingId,
                        PostingParticipantCountProjection::getCnt));

        List<CalendarPostingCardResponse> cards = postingIds.stream()
                .sorted(byExpectedScoreThenTitle(stageByPostingId))
                .map(id -> toCard(stageByPostingId.get(id), participantCountByPostingId))
                .toList();

        return CalendarPostingListResponse.of(date, cards);
    }

    private CalendarPostingCardResponse toCard(PostingStageCalendarProjection stage,
                                               Map<Long, Long> participantCountByPostingId) {
        String displayTitle = stage.getCompanyName() + " " + stage.getTitle();
        return CalendarPostingCardResponse.of(
                stage.getPostingId(),
                displayTitle,
                stage.getStageName(),
                AnnouncementLikelihood.from(stage.getExpectedScore()),
                participantCountByPostingId.getOrDefault(stage.getPostingId(), 0L),
                stage.getLogoImageUrl()
        );
    }

    private Comparator<Long> byExpectedScoreThenTitle(Map<Long, PostingStageCalendarProjection> stageByPostingId) {
        Collator korean = Collator.getInstance(Locale.KOREAN);
        return Comparator
                .comparing((Long id) -> stageByPostingId.get(id).getExpectedScore(), Comparator.reverseOrder())
                .thenComparing(
                        id -> stageByPostingId.get(id).getCompanyName() + " " + stageByPostingId.get(id).getTitle(),
                        korean);
    }

    private void validateRange(YearMonth yearMonth) {
        if (yearMonth.isBefore(MIN) || yearMonth.isAfter(MAX)) {
            throw new CustomException(CalendarErrorCode.UNSUPPORTED_DATE_RANGE);
        }
    }
}