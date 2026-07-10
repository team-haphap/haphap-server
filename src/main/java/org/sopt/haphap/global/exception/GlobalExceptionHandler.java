package org.sopt.haphap.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MissingRequestHeaderException;

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

    private String path(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }

    // === 커스텀 예외 ===
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<FailureResponse> handleCustomException(CustomException e,HttpServletRequest request) {

        ErrorResultCode code = e.getErrorCode();
        // 5xx 계열 커스텀 예외는 실제 장애이므로 스택트레이스와 함께 error 로 남긴다.
        if (code.getStatus().is5xxServerError()) {
            log.error("[{}] {} ({})", path(request), code, code.getStatus(), e);
        } else {
            log.warn("[{}] {} ({})", path(request), code, code.getStatus());
        }

        return buildErrorResponse(code);
    }

    // === 요청 body / 파라미터 검증 ===
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FailureResponse> handleValidationException(MethodArgumentNotValidException e,HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()
                : e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();

        log.warn("[{}] Validation failed: {}", path(request), message);
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<FailureResponse> handleHandlerMethodValidation(HandlerMethodValidationException e,HttpServletRequest request) {
        log.warn("[{}] HandlerMethodValidation failed", path(request));
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    /**
     * @Validated + @PathVariable / @RequestParam 검증 실패.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FailureResponse> handleConstraintViolation(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        log.warn("[{}] Constraint violation: {}", path(request), message);
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE, message);
    }


    /**
     * 주의: e.getMessage() 에는 파싱에 실패한 JSON 원문 조각이 그대로 담긴다.
     * 비밀번호 같은 민감 정보가 로그로 새지 않도록 상세 내용은 debug 로만 남긴다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FailureResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Unreadable message: {}", path(request), e.getClass().getSimpleName());
        log.debug("Unreadable message detail", e);
        return buildErrorResponse(GlobalErrorCode.MESSAGE_NOT_READABLE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailureResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Type mismatch: param={}", path(request), e.getName());
        return buildErrorResponse(GlobalErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<FailureResponse> handleMissingParameter(
            MissingServletRequestParameterException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Missing parameter: {}", path(request), e.getParameterName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<FailureResponse> handleMissingPart(
            MissingServletRequestPartException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Missing request part: {}", path(request), e.getRequestPartName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<FailureResponse> handleMissingCookie(
            MissingRequestCookieException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Missing cookie: {}", path(request), e.getCookieName());
        return buildErrorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<FailureResponse> handleMissingHeader(
            MissingRequestHeaderException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Missing header: {}", path(request), e.getHeaderName());
        return buildErrorResponse(GlobalErrorCode.MISSING_REQUEST_HEADER);
    }

    // ===================== 데이터 무결성 =====================

    /**
     * 주의: e.getMessage() 에 실행된 SQL 과 실제 파라미터 값이 노출된다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<FailureResponse> handleDataIntegrity(
            DataIntegrityViolationException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Data integrity violation: {}", path(request),
                e.getMostSpecificCause().getClass().getSimpleName());
        log.debug("Data integrity violation detail", e);
        return buildErrorResponse(GlobalErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    // ===================== 라우팅 / 메서드 / 미디어 타입 =====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<FailureResponse> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] No resource: {}", path(request), e.getResourcePath());
        return buildErrorResponse(GlobalErrorCode.INVALID_ENDPOINT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<FailureResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Method not supported: {}", path(request), e.getMethod());
        return buildErrorResponse(GlobalErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<FailureResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Unsupported media type: {}", path(request), e.getContentType());
        return buildErrorResponse(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<FailureResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Not acceptable media type", path(request));
        return buildErrorResponse(GlobalErrorCode.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FailureResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        log.warn("[{}] Max upload size exceeded: {} bytes", path(request), e.getMaxUploadSize());
        return buildErrorResponse(GlobalErrorCode.FILE_TOO_LARGE);
    }

    // ===================== 최종 fallback =====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) throws Exception {

        // Spring Security 의 AccessDeniedException / AuthenticationException 을 여기서 하면
        // ExceptionTranslationFilter 에 도달하지 못해 403/401 이 500 으로 나감. -> 반드시 다시 던지기ㅣ.
        if (isSecurityException(e)) {
            throw e;
        }

        log.error("[{}] Unhandled exception", path(request), e);
        return buildErrorResponse(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * spring-security 가 classpath 에 없을 수도 있으므로 직접 import 하지 않고 패키지명으로 판별.
     */
    private boolean isSecurityException(Exception e) {
        return e.getClass().getName().startsWith("org.springframework.security.");
    }
}