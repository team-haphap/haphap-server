package org.sopt.haphap.domain.search.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.support.CategoryParser;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler.Scored;
import org.sopt.haphap.domain.posting.service.support.PostingSortComparators;
import org.sopt.haphap.domain.search.code.SearchErrorCode;
import org.sopt.haphap.domain.search.domain.RelatedSearchKeyword;
import org.sopt.haphap.domain.search.dto.PostingSearchCondition;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.sopt.haphap.domain.search.dto.SearchPostingResponse;
import org.sopt.haphap.domain.search.repository.RelatedSearchKeywordRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingSearchQueryService {

    private final PostingRepository postingRepository;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;
    private final CategoryParser categoryParser;
    private final RelatedSearchKeywordRepository relatedSearchKeywordRepository;

    public SearchPostingListResponse search(
            String q, Long relatedKeywordId, List<String> category, Integer page, Integer size
    ) {
        String resolvedKeyword = resolveKeyword(q, relatedKeywordId);
        List<String> categories = categoryParser.parse(category);

        PostingSearchCondition condition = PostingSearchCondition.of(resolvedKeyword, categories, page, size);
        validateKeyword(condition.keyword());

        List<Long> postingIds = postingRepository.searchPostingIds(
                condition.keyword(), condition.categories());

        if (postingIds.isEmpty()) {
            return SearchPostingListResponse.of(List.of(), condition.page(), condition.size(), false);
        }

        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.LISTING);

        List<Scored> sorted = postingIds.stream()
                .map(id -> assembler.assemble(agg.posting(id), agg.stages(id), agg.counts(id), agg.companyImageUrl(id)))
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

    private String resolveKeyword(String q, Long relatedKeywordId) {
        if (relatedKeywordId == null) {
            return q;
        }
        RelatedSearchKeyword keyword = relatedSearchKeywordRepository.findByIdAndIsActiveTrue(relatedKeywordId)
                .orElseThrow(() -> new CustomException(SearchErrorCode.RELATED_KEYWORD_NOT_FOUND));
        return keyword.getKeyword();
    }

    private void validateKeyword(String keyword) {
        if (keyword == null) {
            throw new CustomException(SearchErrorCode.KEYWORD_REQUIRED);
        }
    }

    private SearchPostingResponse toSearchResponse(Scored scored) {
        PopularPostingResponse r = scored.response();
        return new SearchPostingResponse(
                r.id(), r.companyName(), r.title(), r.category(),
                r.nextStage(), r.daysUntilNextStage(), r.imageUrl());
    }
}