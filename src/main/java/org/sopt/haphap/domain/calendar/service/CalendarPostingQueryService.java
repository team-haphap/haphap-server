package org.sopt.haphap.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingCardResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarPostingQueryService {

    private final PostingStageRepository postingStageRepository;
    private final PostingRepository postingRepository;
    private final PostingViewTracker postingViewTracker;

    public CalendarPostingListResponse getPostingsByDate(LocalDate date) {
        List<PostingStageCalendarProjection> stages =
                postingStageRepository.findCalendarStagesByDate(date);

        if (stages.isEmpty()) {
            return CalendarPostingListResponse.of(date, List.of());
        }

        Map<Long, PostingStageCalendarProjection> stageByPostingId = stages.stream()
                .collect(Collectors.toMap(
                        PostingStageCalendarProjection::getPostingId,
                        Function.identity(),
                        this::pickHigherScore));

        List<Long> postingIds = List.copyOf(stageByPostingId.keySet());

        Map<Long, String> titleByPostingId = postingRepository.findSummariesByIds(postingIds).stream()
                .collect(Collectors.toMap(PostingSummaryResponse::id, PostingSummaryResponse::title));

        Map<Long, Long> viewCountByPostingId = postingViewTracker.getViewCounts(postingIds);

        List<CalendarPostingCardResponse> cards = postingIds.stream()
                .sorted(byExpectedScoreThenTitle(stageByPostingId, titleByPostingId))
                .map(id -> toCard(id, stageByPostingId.get(id), titleByPostingId, viewCountByPostingId))
                .toList();

        return CalendarPostingListResponse.of(date, cards);
    }

    private PostingStageCalendarProjection pickHigherScore(PostingStageCalendarProjection a, PostingStageCalendarProjection b) {
        return a.getExpectedScore() >= b.getExpectedScore() ? a : b;
    }

    private CalendarPostingCardResponse toCard(Long postingId,
                                               PostingStageCalendarProjection stage,
                                               Map<Long, String> titleByPostingId,
                                               Map<Long, Long> viewCountByPostingId) {
        return new CalendarPostingCardResponse(
                postingId,
                titleByPostingId.getOrDefault(postingId, ""),
                stage.getStageName(),
                AnnouncementLikelihood.from(stage.getExpectedScore()),
                viewCountByPostingId.getOrDefault(postingId, 0L)
        );
    }

    // Collator는 스레드 안전하지 않아 공유 필드로 두지 않고 호출마다 로컬 생성 - (동시성 고려)
    private Comparator<Long> byExpectedScoreThenTitle(Map<Long, PostingStageCalendarProjection> stageByPostingId,
                                                      Map<Long, String> titleByPostingId) {
        Collator korean = Collator.getInstance(Locale.KOREAN);
        return Comparator
                .comparing((Long id) -> stageByPostingId.get(id).getExpectedScore(), Comparator.reverseOrder())
                .thenComparing(id -> titleByPostingId.getOrDefault(id, ""), korean);
    }
}