package org.sopt.haphap.domain.passcard.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PassCardArchiveListResponse(
        List<PassCardArchiveItemResponse> cards,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isLast) {

    public static PassCardArchiveListResponse from(Page<PassCardArchiveItemResponse> page) {
        return new PassCardArchiveListResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}