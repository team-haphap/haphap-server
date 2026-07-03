package org.sopt.haphap.domain.search.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.search.code.SearchSuccessCode;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingListResponse;
import org.sopt.haphap.domain.search.service.AutocompleteService;
import org.sopt.haphap.domain.search.service.PopularSearchPostingQueryService;
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

    @GetMapping("/popular")
    public ResponseEntity<SuccessResponse<PopularSearchPostingListResponse>> getPopularPostings() {
        PopularSearchPostingListResponse response = popularSearchPostingQueryService.getPopularPostings();

        SuccessResponse<PopularSearchPostingListResponse> body =
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
}