package org.sopt.haphap.global.dto;

import java.util.List;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public record ValidationFailureResponse(
        int status,
        String code,
        String message,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(String field, Object rejectedValue, String reason) {
        public static FieldErrorDetail from(FieldError fieldError) {
            return new FieldErrorDetail(
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage());
        }
    }

    public static ValidationFailureResponse of(ErrorResultCode errorCode, BindingResult bindingResult) {
        List<FieldErrorDetail> details = bindingResult.getFieldErrors().stream()
                .map(FieldErrorDetail::from)
                .toList();
        return new ValidationFailureResponse(
                errorCode.getStatus().value(),
                errorCode.toString(),
                errorCode.getMessage(),
                details);
    }
}