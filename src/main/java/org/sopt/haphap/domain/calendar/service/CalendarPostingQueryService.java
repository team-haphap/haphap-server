package org.sopt.haphap.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingCardResponse;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.dto.StagePendingCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
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
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarPostingQueryService {

    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;

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
                        CalendarStageMerger::pickHigherScore));

        List<Long> postingIds = List.copyOf(stageByPostingId.keySet());

        // 전형 순서 배치 조회 (이전 단계 판별용, 공고 수와 무관하게 쿼리 1번)
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));

        // 대표 전형 바로 이전 전형(orderIndex - 1)의 stageId (없으면 첫 전형이라 null)
        Map<Long, Long> previousStageIdByPostingId = new HashMap<>();
        for (Long postingId : postingIds) {
            Long previousStageId = findPreviousStageId(stageByPostingId.get(postingId), stagesByPosting.get(postingId));
            if (previousStageId != null) {
                previousStageIdByPostingId.put(postingId, previousStageId);
            }
        }
        List<Long> previousStageIds = List.copyOf(previousStageIdByPostingId.values());

        // 참여중 인원 = 이전 전형에 상태등록(합격+탈락, 대기중 제외)한 사람 수
        Map<Long, Long> statusRegisteredCountByStageId = previousStageIds.isEmpty()
                ? Map.of()
                : registrationRepository
                .countByStageIdsAndResult(previousStageIds, List.of(RegistrationResult.PASS, RegistrationResult.FAIL)).stream()
                .collect(Collectors.toMap(StagePendingCountProjection::getStageId, StagePendingCountProjection::getCnt));

        List<CalendarPostingCardResponse> cards = postingIds.stream()
                .sorted(byExpectedScoreThenTitle(stageByPostingId))
                .map(id -> toCard(stageByPostingId.get(id), previousStageIdByPostingId.get(id), statusRegisteredCountByStageId))
                .toList();

        return CalendarPostingListResponse.of(date, cards);
    }

    private Long findPreviousStageId(PostingStageCalendarProjection representative,
                                     List<PostingStageFlatProjection> postingStages) {
        if (postingStages == null) {
            return null;
        }
        int targetOrder = representative.getOrderIndex() - 1;
        return postingStages.stream()
                .filter(s -> s.getOrderIndex() == targetOrder)
                .map(PostingStageFlatProjection::getStageId)
                .findFirst()
                .orElse(null);
    }

    private CalendarPostingCardResponse toCard(PostingStageCalendarProjection stage,
                                               Long previousStageId,
                                               Map<Long, Long> statusRegisteredCountByStageId) {
        long participantCount = previousStageId == null ? 0L : statusRegisteredCountByStageId.getOrDefault(previousStageId, 0L);
        return CalendarPostingCardResponse.of(
                stage.getPostingId(),
                stage.getTitle(),
                stage.getStageName(),
                AnnouncementLikelihood.from(stage.getExpectedScore()),
                participantCount,
                stage.getCompanyImageUrl()
        );
    }

    private Comparator<Long> byExpectedScoreThenTitle(Map<Long, PostingStageCalendarProjection> stageByPostingId) {
        Collator korean = Collator.getInstance(Locale.KOREAN);
        return Comparator
                .comparing((Long id) -> stageByPostingId.get(id).getExpectedScore(), Comparator.reverseOrder())
                .thenComparing(id -> stageByPostingId.get(id).getTitle(), korean);
    }
}