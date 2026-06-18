package org.sopt.haphap.global.exception;

import lombok.Getter;
import org.sopt.haphap.global.code.ErrorResultCode;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorResultCode errorCode;

    public CustomException(ErrorResultCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
