package org.sopt.haphap.domain.passcard.dto.response;

import java.util.List;

public record PassCardArchiveListResponse(List<PassCardArchiveItemResponse> passCards) {

    public static PassCardArchiveListResponse from(List<PassCardArchiveItemResponse> passCards) {
        return new PassCardArchiveListResponse(passCards);
    }
}