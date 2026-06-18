package org.sopt.haphap.global.code;

import org.springframework.http.HttpStatus;

public interface ResultCode {
    HttpStatus getStatus();
    String getMessage();
    String name();
}