package org.sopt.haphap.domain.posting.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.PostingActivityProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularPostingService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final NextStageCalculator nextStageCalculator;

    public PopularPostingListResponse getPopularPostings(List<String> categoryNames) {
        // "전체"이거나 비어있으면 null로 정규화 → 필터 미적용
        List<String> filter = (categoryNames == null || categoryNames.isEmpty())
                ? null : categoryNames;

        // 1단계: 활동 있는 공고 id를 활동시각 최신순으로
        List<PostingActivityProjection> activities =
                postingRepository.findActivePostingIdsByCategories(filter);
        if (activities.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }

        // 활동시각 정렬 순서를 보존한 id 리스트
        List<Long> orderedIds = activities.stream()
                .map(PostingActivityProjection::getPostingId)
                .toList();

        // 2단계: 배치 조회
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(orderedIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        // 공고별 전형 목록 (orderIndex 순 — 쿼리에서 정렬해둠)
        Map<Long, List<PostingStage>> stagesByPosting = postingStageRepository
                .findAllByPostingIds(orderedIds).stream()
                .collect(Collectors.groupingBy(s -> s.getPosting().getId()));

        // (공고, 전형)별 등록수 → 공고별 { stageId -> count }
        Map<Long, Map<Long, Long>> countsByPosting = registrationRepository
                .countByPostingAndStage(orderedIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        // 활동시각 순서(orderedIds)를 유지하며 응답 조립
        List<PopularPostingResponse> responses = orderedIds.stream()
                .map(id -> toResponse(
                        postingMap.get(id),
                        stagesByPosting.getOrDefault(id, List.of()),
                        countsByPosting.getOrDefault(id, Map.of())))
                .toList();

        return PopularPostingListResponse.from(responses);
    }

    private PopularPostingResponse toResponse(Posting posting,
                                              List<PostingStage> stages,
                                              Map<Long, Long> countByStageId) {
        PostingStage nextStage = nextStageCalculator.calculate(stages, countByStageId);
        Integer days = nextStageCalculator.daysUntil(nextStage);

        return new PopularPostingResponse(
                posting.getId(),
                posting.getTitle(),
                posting.getCompany().getName(),
                posting.getCategory().getName(),
                posting.getCompany().getDescription(),
                nextStage == null ? null : nextStage.getName(),
                days,
                posting.getCompany().getImageUrl());
    }
}
