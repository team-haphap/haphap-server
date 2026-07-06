package org.sopt.haphap.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 응답 빌더
    private ResponseEntity<FailureResponse> buildErrorResponse(ErrorResultCode errorCode) {
        FailureResponse response = ApiResponse.failure(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    private ResponseEntity<FailureResponse> buildErrorResponse(ErrorResultCode errorCode, String message) {
        FailureResponse response = ApiResponse.failure(errorCode, message);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // === 커스텀 예외 ===
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<FailureResponse> handleCustomException(CustomException e) {
        log.warn("CustomException: {}", e.getMessage());
        return buildErrorResponse(e.getErrorCode());
    }

    // === 요청 body / 파라미터 검증 ===
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FailureResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("Validation failed: {}", message);
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<FailureResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.warn("HandlerMethodValidation failed: {}", e.getMessage());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FailureResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Unreadable message: {}", e.getMessage());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailureResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: param={}, value={}", e.getName(), e.getValue());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<FailureResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<FailureResponse> handleMissingPart(MissingServletRequestPartException e) {
        log.warn("Missing request part: {}", e.getRequestPartName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<FailureResponse> handleMissingCookie(MissingRequestCookieException e) {
        log.warn("Missing cookie: {}", e.getCookieName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    // === 라우팅 / 메서드 ===
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<FailureResponse> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("No resource: {}", e.getResourcePath());
        return buildErrorResponse(GlobalErrorCode.INVALID_ENDPOINT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<FailureResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMethod());
        return buildErrorResponse(GlobalErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception - {} {}", request.getMethod(), request.getRequestURI(), e);
        return buildErrorResponse(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FailureResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("Max upload size exceeded: {}", e.getMessage());
        return buildErrorResponse(GlobalErrorCode.FILE_TOO_LARGE);
    }
}