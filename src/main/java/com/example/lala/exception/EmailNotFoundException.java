package com.example.lala.exception;

/**
 * 이메일을 찾을 수 없을 때 발생하는 예외 클래스
 * 사용자 조회, 이메일 인증 등에서 해당 이메일이 존재하지 않을 때 사용
 */
public class EmailNotFoundException extends RuntimeException {

    /**
     * 메시지와 함께 EmailNotFoundException 생성
     * @param message 예외 메시지
     */
    public EmailNotFoundException(String message) {
        super(message);
    }
}