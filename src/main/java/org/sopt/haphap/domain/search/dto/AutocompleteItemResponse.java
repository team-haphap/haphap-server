package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteItemResponse(
        AutocompleteType type,
        String name,
        List<HighlightRange> highlightRanges,
        String url
) {
    public static AutocompleteItemResponse company(Long id, String name, List<HighlightRange> ranges) {
        return new AutocompleteItemResponse(AutocompleteType.COMPANY, name, ranges, "/companies/" + id);
    }

    public static AutocompleteItemResponse job(Long id, String name, List<HighlightRange> ranges) {
        return new AutocompleteItemResponse(AutocompleteType.JOB, name, ranges, "/postings/" + id);
    }
}

//이 부분 회사 상세 화면 관련한 기능명세, 화면설계서 보고 업데이트해야 합니당