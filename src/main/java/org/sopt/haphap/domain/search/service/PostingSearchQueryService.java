package org.sopt.haphap.domain.search.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PopularPostingResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.PostingAggregate;
import org.sopt.haphap.domain.posting.service.PostingAggregateLoader;
import org.sopt.haphap.domain.posting.service.PostingResponseAssembler;
import org.sopt.haphap.domain.posting.service.PostingResponseAssembler.Scored;
import org.sopt.haphap.domain.posting.service.PostingSortComparators;
import org.sopt.haphap.domain.search.dto.PostingSearchCondition;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingSearchQueryService {

    private final PostingRepository postingRepository;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;

    public SearchPostingListResponse search(PostingSearchCondition condition) {
        List<Posting> matched = postingRepository.searchPostings(
                condition.keyword(), condition.categories(), condition.status());

        if (matched.isEmpty()) {
            return SearchPostingListResponse.of(List.of(), condition.page(), condition.size(), false);
        }

        List<Long> postingIds = matched.stream().map(Posting::getId).toList();
        PostingAggregate agg = aggregateLoader.load(postingIds);

        List<Scored> sorted = postingIds.stream()
                .map(id -> assembler.assemble(agg.posting(id), agg.stages(id), agg.counts(id)))
                .sorted(PostingSortComparators.byAnnounceDate())
                .toList();

        int from = Math.min(condition.page() * condition.size(), sorted.size());
        int to = Math.min(from + condition.size(), sorted.size());
        boolean hasNext = to < sorted.size();

        List<PopularPostingResponse> pageContent = sorted.subList(from, to).stream()
                .map(Scored::response)
                .toList();

        return SearchPostingListResponse.of(pageContent, condition.page(), condition.size(), hasNext);
    }
}