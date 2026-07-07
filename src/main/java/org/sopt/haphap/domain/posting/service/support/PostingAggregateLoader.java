package org.sopt.haphap.domain.posting.service.support;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingAggregateLoader {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;

    /**
     * 주어진 공고 id들에 대해 계산에 필요한 데이터(공고·전형·누적등록수)를 배치로 로딩.
     * 전형 목록은 orderIndex 오름차순으로 정렬해 담으ㅁ.
     */
    public PostingAggregate load(List<Long> postingIds) {
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(postingIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(
                        Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));

        Map<Long, Map<Long, Long>> countsByPosting = stageResultCountRepository
                .findTotalsByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));
        return new PostingAggregate(postingMap, stagesByPosting, countsByPosting);
    }
}