package org.sopt.haphap.domain.posting.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.support.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingListingService {

    private final PostingRepository postingRepository;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;
    private final CategoryParser categoryParser;

    public PopularPostingListResponse getAllPostings(List<String> category) {

        List<String> categoryNames = categoryParser.parse(category);
        List<String> filter = (categoryNames == null || categoryNames.isEmpty()) ? null : categoryNames;

        // 1) 카테고리로 전체 공고 (회사·카테고리 fetch join)
        List<Posting> postings = postingRepository.findAllWithCompanyAndCategory(filter);
        if (postings.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }
        List<Long> postingIds = postings.stream().map(Posting::getId).toList();
        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.LISTING);

        List<PopularPostingResponse> result = postingIds.stream()
                .map(id -> assembler.assemble(agg.posting(id), agg.stages(id), agg.counts(id), agg.companyImageUrl(id)))
                .sorted(PostingSortComparators.byAnnounceDate())
                .map(PostingResponseAssembler.Scored::response)
                .toList();

        return PopularPostingListResponse.from(result);
    }
}