package org.sopt.haphap.domain.home.dto.response;

import java.util.List;

public record MyApplicationListResponse(List<MyApplicationResponse> applications) {

    public static MyApplicationListResponse from(List<MyApplicationResponse> applications) {
        return new MyApplicationListResponse(applications);
    }
}
