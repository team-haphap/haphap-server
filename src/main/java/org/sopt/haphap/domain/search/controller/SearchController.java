package org.sopt.haphap.domain.search.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.service.support.CategoryParser;
import org.sopt.haphap.domain.search.code.SearchSuccessCode;
import org.sopt.haphap.domain.search.dto.*;
import org.sopt.haphap.domain.search.keyword.PopularSearchKeywordQueryService;
import org.sopt.haphap.domain.search.keyword.SearchExecutionService;
import org.sopt.haphap.domain.search.service.AutocompleteService;
import org.sopt.haphap.domain.search.service.PopularSearchPostingQueryService;
import org.sopt.haphap.domain.search.service.PostingSearchQueryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController implements SearchApiDocs {

    private final PopularSearchPostingQueryService popularSearchPostingQueryService;
    private final AutocompleteService autocompleteService;
    private final PostingSearchQueryService postingSearchQueryService;
    private final PopularSearchKeywordQueryService popularSearchKeywordQueryService;
    private final SearchExecutionService searchExecutionService;

    @GetMapping("/popular")
    public ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings() {
        PopularPostingListResponse response = popularSearchPostingQueryService.getPopularPostings();

        SuccessResponse<PopularPostingListResponse> body =
                ApiResponse.success(SearchSuccessCode.POPULAR_POSTINGS_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<SuccessResponse<AutocompleteResponse>> autocomplete(
            @RequestParam(required = false) String q
    ) {
        AutocompleteResponse response = autocompleteService.autocomplete(q);
        SuccessResponse<AutocompleteResponse> body =
                ApiResponse.success(SearchSuccessCode.AUTOCOMPLETE_FETCHED, response);
        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/postings")
    public ResponseEntity<SuccessResponse<SearchPostingListResponse>> searchPostings(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long relatedKeywordId,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        SearchPostingListResponse response =
                postingSearchQueryService.search(q, relatedKeywordId, category, page, size);

        SuccessResponse<SearchPostingListResponse> body =
                ApiResponse.success(SearchSuccessCode.POSTING_SEARCH_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/popular-keywords")
    public ResponseEntity<SuccessResponse<PopularKeywordListResponse>> getPopularKeywords() {
        SuccessResponse<PopularKeywordListResponse> body = ApiResponse.success(
                SearchSuccessCode.POPULAR_KEYWORDS_FETCHED,
                popularSearchKeywordQueryService.getPopularKeywords());
        return ResponseEntity.status(body.status()).body(body);
    }

    @PostMapping("/executions")
    public ResponseEntity<Void> recordExecution(@RequestBody SearchExecutionRequest request) {
        searchExecutionService.record(request.keyword());
        return ResponseEntity.noContent().build();
    }
}