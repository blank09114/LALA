package com.example.lala.exception;

/**
 * 유효하지 않은 인증 코드로 인해 발생하는 예외 클래스
 * 이메일 인증 과정에서 잘못된 인증 코드를 입력했을 때 사용
 */
public class InvalidVerificationCodeException extends RuntimeException {

    /**
     * 메시지와 함께 InvalidVerificationCodeException 생성
     * @param message 예외 메시지
     */
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}