package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteResponse(List<AutocompleteItemResponse> results) {

    public static AutocompleteResponse from(List<AutocompleteItemResponse> results) {
        return new AutocompleteResponse(results);
    }
}