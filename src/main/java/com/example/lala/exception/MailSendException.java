package com.example.lala.exception;

/**
 * 이메일 전송 실패 시 발생하는 예외 클래스
 * SMTP 서버 오류, 네트워크 문제 등으로 이메일 전송에 실패했을 때 사용
 */
public class MailSendException extends RuntimeException {

    /**
     * 메시지와 함께 MailSendException 생성
     * @param message 예외 메시지
     */
    public MailSendException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외와 함께 MailSendException 생성
     * @param message 예외 메시지
     * @param cause 원인이 된 예외
     */
    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}