package com.example.lala.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러 클래스
 * 애플리케이션에서 발생하는 다양한 예외들을 일관된 형태로 처리하여 클라이언트에게 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 에러 응답의 공통 타임스탬프 키 */
    private static final String TIMESTAMP_KEY = "timestamp";

    /** 에러 응답의 공통 에러 키 */
    private static final String ERROR_KEY = "error";

    /** 에러 응답의 공통 메시지 키 */
    private static final String MESSAGE_KEY = "message";

    /**
     * OAuth2 제공자 불일치 예외 처리
     * @param exception 발생한 ProviderMismatchException
     * @return 에러 응답
     */
    @ExceptionHandler(ProviderMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleProviderMismatch(ProviderMismatchException exception) {
        log.error("Provider mismatch error: {}", exception.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "PROVIDER_MISMATCH",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 이메일 발송 실패 예외 처리
     * @param exception 발생한 MailSendException
     * @return 에러 응답
     */
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<Map<String, Object>> handleMailSendException(MailSendException exception) {
        log.error("Mail send error: {}", exception.getMessage(), exception);

        Map<String, Object> errorResponse = createErrorResponse(
                "MAIL_SEND_FAILED",
                "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 이메일을 찾을 수 없는 예외 처리
     * @param exception 발생한 EmailNotFoundException
     * @return 에러 응답
     */
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotFoundException(EmailNotFoundException exception) {
        log.error("Email not found: {}", exception.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "EMAIL_NOT_FOUND",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 유효하지 않은 인증 코드 예외 처리
     * @param exception 발생한 InvalidVerificationCodeException
     * @return 에러 응답
     */
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidVerificationCode(InvalidVerificationCodeException exception) {
        log.error("Invalid verification code: {}", exception.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "INVALID_VERIFICATION_CODE",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 유효성 검사 실패 예외 처리
     * @param exception 발생한 MethodArgumentNotValidException
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        log.error("Validation error: {}", exception.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorResponse = createErrorResponse(
                "VALIDATION_FAILED",
                "입력값이 올바르지 않습니다."
        );
        errorResponse.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 일반적인 런타임 예외 처리
     * @param exception 발생한 RuntimeException
     * @return 에러 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException exception) {
        log.error("Runtime error: {}", exception.getMessage(), exception);

        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 모든 예외를 처리하는 최종 핸들러
     * @param exception 발생한 Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception) {
        log.error("Unexpected error: {}", exception.getMessage(), exception);

        Map<String, Object> errorResponse = createErrorResponse(
                "UNEXPECTED_ERROR",
                "예상치 못한 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 에러 응답 객체 생성 헬퍼 메서드
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @return 에러 응답 맵
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, errorCode);
        errorResponse.put(MESSAGE_KEY, message);
        errorResponse.put(TIMESTAMP_KEY, System.currentTimeMillis());
        return errorResponse;
    }
}