package org.sopt.haphap.domain.mypage.dto.response;

import java.util.List;

public record MyRegistrationPreviewListResponse(List<MyRegistrationPreviewResponse> registrations) {

    public static MyRegistrationPreviewListResponse from(List<MyRegistrationPreviewResponse> registrations) {
        return new MyRegistrationPreviewListResponse(registrations);
    }
}
