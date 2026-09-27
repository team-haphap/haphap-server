package org.sopt.haphap.domain.posting.service.support;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.CompanyImageRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingAggregateLoader {

    private final PostingRepository postingRepository;
    private final CompanyImageRepository companyImageRepository;

    public PostingAggregate load(List<Long> postingIds, CompanyImageType imageType) {
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(postingIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        List<Long> companyIds = postingMap.values().stream()
                .map(p -> p.getCompany().getId())
                .distinct()
                .toList();

        Map<Long, String> companyImageByCompanyId = companyImageRepository
                .findByCompanyIdInAndType(companyIds, imageType).stream()
                .collect(Collectors.toMap(
                        ci -> ci.getCompany().getId(), CompanyImage::getImageUrl));

        return new PostingAggregate(postingMap, companyImageByCompanyId);
    }
}
