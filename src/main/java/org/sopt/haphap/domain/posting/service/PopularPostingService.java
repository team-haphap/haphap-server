package org.sopt.haphap.domain.posting.service;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularPostingService {

    private static final int RECENT_HOURS = 48;
    private static final List<RegistrationResult> COUNTED_RESULTS =
            List.of(RegistrationResult.PASS, RegistrationResult.FAIL);

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final NextStageCalculator nextStageCalculator;

    public PopularPostingListResponse getPopularPostings(List<String> categoryNames) {
        // "전체"이거나 비어있으면 null로 정규화 → 필터 미적용
        List<String> filter = (categoryNames == null || categoryNames.isEmpty())
                ? null : categoryNames;
        LocalDateTime since = LocalDateTime.now().minusHours(RECENT_HOURS);

        // 1) 48h 내 PASS/FAIL 결과 있는 공고 id목록
        List<Long> postingIds = registrationRepository
                .findRecentlyActivePostingIds(COUNTED_RESULTS, since, filter);
        if (postingIds.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }

        // 2) 배치 조회
        // 공고+회사+카테고리
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(postingIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        // 공고별 전형 목록 (orderIndex 순 — 쿼리에서 정렬해둠)
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));


        // (공고, 전형)별 등록수 → 공고별 { stageId -> count }
        Map<Long, Map<Long, Long>> countsByPosting = registrationRepository
                .countByPostingAndStage(postingIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        // 3) 각 공고 → 응답 + 정렬키(nextStage 발표일) 계산
        List<ScoredPosting> scored = postingIds.stream()
                .map(id -> buildScored(
                        postingMap.get(id),
                        stagesByPosting.getOrDefault(id, List.of()),
                        countsByPosting.getOrDefault(id, Map.of())))
                .toList();

        // 4) 앱에서 최종 정렬: 발표일 가까운 순(null·과거 뒤), 같으면 공고명 가나다순
        Collator korean = Collator.getInstance(Locale.KOREAN);
        List<PopularPostingResponse> result = scored.stream()
                .sorted(sortComparator(korean))
                .map(ScoredPosting::response)
                .toList();

        return PopularPostingListResponse.from(result);
    }

    private ScoredPosting buildScored(Posting posting,
                                      List<PostingStageFlatProjection> stages,
                                      Map<Long, Long> countByStageId) {
        PostingStageFlatProjection nextStage = nextStageCalculator.calculate(stages, countByStageId);
        Integer days = nextStageCalculator.daysUntil(nextStage);
        LocalDate announceDate = (nextStage == null) ? null : nextStage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(),
                posting.getTitle(),
                posting.getCompany().getName(),
                posting.getCategory().getName(),
                posting.getCompany().getDescription(),
                nextStage == null ? null : nextStage.getName(),
                days,
                posting.getCompany().getImageUrl());

        return new ScoredPosting(response, posting.getTitle(), announceDate);
    }

    // 발표일: 미래 가까운 순 → 과거 → null 순으로 뒤로. 동일 발표일이면 공고명 가나다.
    private Comparator<ScoredPosting> sortComparator(Collator korean) {
        LocalDate today = LocalDate.now();
        return Comparator
                .comparing((ScoredPosting s) -> sortRank(s.announceDate(), today)) // 0=미래,1=과거,2=null
                .thenComparing(ScoredPosting::announceDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ScoredPosting::title, korean);
    }

    private int sortRank(LocalDate date, LocalDate today) {
        if (date == null) return 2;
        return date.isBefore(today) ? 1 : 0;
    }

    private record ScoredPosting(PopularPostingResponse response, String title, LocalDate announceDate) {
    }
}

