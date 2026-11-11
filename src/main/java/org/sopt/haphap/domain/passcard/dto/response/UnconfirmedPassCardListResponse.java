package org.sopt.haphap.domain.passcard.dto.response;

import java.util.List;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;

public record UnconfirmedPassCardListResponse(List<PassCardResponse> passCards) {

    public static UnconfirmedPassCardListResponse from(List<PassCardResponse> passCards) {
        return new UnconfirmedPassCardListResponse(passCards);
    }
}