package org.sopt.haphap.domain.posting.service.support;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.CompanyImageRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingAggregateLoader {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final CompanyImageRepository companyImageRepository;

    public PostingAggregate load(List<Long> postingIds, CompanyImageType imageType) {
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

        List<Long> companyIds = postingMap.values().stream()
                .map(p -> p.getCompany().getId())
                .distinct()
                .toList();

        Map<Long, String> companyImageByCompanyId = companyImageRepository
                .findByCompanyIdInAndType(companyIds, imageType).stream()
                .collect(Collectors.toMap(
                        ci -> ci.getCompany().getId(), CompanyImage::getImageUrl));

        return new PostingAggregate(postingMap, stagesByPosting, countsByPosting, companyImageByCompanyId);
    }
}