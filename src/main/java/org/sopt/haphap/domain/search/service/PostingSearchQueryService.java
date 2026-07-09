package org.sopt.haphap.domain.search.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.PostingAggregate;
import org.sopt.haphap.domain.posting.service.PostingAggregateLoader;
import org.sopt.haphap.domain.posting.service.PostingResponseAssembler;
import org.sopt.haphap.domain.posting.service.PostingResponseAssembler.Scored;
import org.sopt.haphap.domain.posting.service.PostingSortComparators;
import org.sopt.haphap.domain.search.code.SearchErrorCode;
import org.sopt.haphap.domain.search.dto.PostingSearchCondition;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.sopt.haphap.domain.search.dto.SearchPostingResponse;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingSearchQueryService {

    private final PostingRepository postingRepository;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;
    private final CategoryRepository categoryRepository;

    public SearchPostingListResponse search(PostingSearchCondition condition) {
        validateCategory(condition.category());

        List<Long> postingIds = postingRepository.searchPostingIds(
                condition.keyword(), condition.category());

        if (postingIds.isEmpty()) {
            return SearchPostingListResponse.of(List.of(), condition.page(), condition.size(), false);
        }

        PostingAggregate agg = aggregateLoader.load(postingIds);

        List<Scored> sorted = postingIds.stream()
                .map(id -> assembler.assemble(agg.posting(id), agg.stages(id), agg.counts(id)))
                .sorted(PostingSortComparators.byDeadline())
                .toList();

        int totalSize = sorted.size();
        long fromLong = (long) condition.page() * condition.size();
        int from = (int) Math.min(fromLong, totalSize);
        int to = (int) Math.min(fromLong + condition.size(), totalSize);
        boolean hasNext = to < totalSize;

        List<SearchPostingResponse> pageContent = sorted.subList(from, to).stream()
                .map(this::toSearchResponse)
                .toList();

        return SearchPostingListResponse.of(pageContent, condition.page(), condition.size(), hasNext);
    }

    private void validateCategory(String category) {
        if (category != null && !categoryRepository.existsByName(category)) {
            throw new CustomException(SearchErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    private SearchPostingResponse toSearchResponse(Scored scored) {
        PopularPostingResponse r = scored.response();
        return new SearchPostingResponse(
                r.id(), r.companyName(), r.title(), r.category(),
                r.nextStage(), r.daysUntilNextStage(), r.imageUrl());
    }
}