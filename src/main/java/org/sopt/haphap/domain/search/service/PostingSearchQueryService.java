package org.sopt.haphap.domain.search.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.service.support.*;
import org.sopt.haphap.domain.search.code.SearchErrorCode;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler.Scored;
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
    private final CategoryParser categoryParser;

    public SearchPostingListResponse search(PostingSearchCondition condition) {
        validateKeyword(condition.keyword());
        List<String> categories =
                categoryParser.parse(condition.categories());

        List<Long> postingIds = postingRepository.searchPostingIds(
                condition.keyword(),categories);

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

    private void validateKeyword(String keyword) {
        // PostingSearchCondition.of()가 이미 blank → null로 정규화해줌
        if (keyword == null) {
            throw new CustomException(SearchErrorCode.KEYWORD_REQUIRED);
        }
    }
/*
    private void validateCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) return;
        List<String> distinct = categories.stream().distinct().toList();
        long existingCount = categoryRepository.countByNameIn(distinct);
        if (existingCount != distinct.size()) {
            throw new CustomException(SearchErrorCode.CATEGORY_NOT_FOUND);
        }
    }

 */

    private SearchPostingResponse toSearchResponse(Scored scored) {
        PopularPostingResponse r = scored.response();
        return new SearchPostingResponse(
                r.id(), r.companyName(), r.title(), r.category(),
                r.nextStage(), r.daysUntilNextStage(), r.imageUrl());
    }
}