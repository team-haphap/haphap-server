package org.sopt.haphap.domain.search.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.search.code.SearchSuccessCode;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.PostingSearchCondition;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.sopt.haphap.domain.search.service.AutocompleteService;
import org.sopt.haphap.domain.search.service.PopularSearchPostingQueryService;
import org.sopt.haphap.domain.search.service.PostingSearchQueryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController implements SearchApiDocs {

    private final PopularSearchPostingQueryService popularSearchPostingQueryService;
    private final AutocompleteService autocompleteService;
    private final PostingSearchQueryService postingSearchQueryService;

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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PostingSearchCondition condition = PostingSearchCondition.of(q, category, page, size);
        SearchPostingListResponse response = postingSearchQueryService.search(condition);

        SuccessResponse<SearchPostingListResponse> body =
                ApiResponse.success(SearchSuccessCode.POSTING_SEARCH_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }
}