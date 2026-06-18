package org.sopt.haphap.global.dto;

import org.sopt.haphap.global.code.ErrorResultCode;
import org.sopt.haphap.global.code.SuccessResultCode;

public interface ApiResponse {

    int status();

    String code();

    String message();

    static <T> SuccessResponse<T> success(SuccessResultCode successCode, T data) {
        return SuccessResponse.of(successCode, data);
    }

    static <T> SuccessResponse<T> success(SuccessResultCode successCode) {
        return SuccessResponse.of(successCode);
    }

    static FailureResponse failure(ErrorResultCode errorCode) {
        return FailureResponse.of(errorCode);
    }

    static FailureResponse failure(ErrorResultCode errorCode, String message) {
        return FailureResponse.of(errorCode, message);
    }
}