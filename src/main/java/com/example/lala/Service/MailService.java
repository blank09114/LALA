package com.example.lala.Service;

import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.CompletableFuture;

/**
 * 이메일 서비스 인터페이스
 * 사용자 인증과 비밀번호 재설정을 위한 이메일 발송 및 검증 기능을 제공합니다.
 * 회원가입 시 이메일 인증과 비밀번호 찾기 기능에서 사용됩니다.
 */
public interface MailService {

    /**
     * 이메일 인증을 위한 메일 메시지를 생성합니다.
     * 
     * @param mail 인증 메일을 받을 이메일 주소
     * @return 생성된 MIME 메시지 객체
     */
    MimeMessage createMail(String mail);

    /**
     * 사용자가 입력한 인증 코드를 검증합니다.
     * 
     * @param email 인증을 요청한 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 시 true, 실패 시 false
     */
    boolean verifyCode(String email, int code);

    /**
     * 이메일 인증 메일을 비동기적으로 발송합니다.
     * 
     * @param mail 인증 메일을 받을 이메일 주소
     * @return 생성된 인증 코드를 담은 CompletableFuture 객체
     */
    CompletableFuture<Integer> sendMail(String mail);

    /**
     * 비밀번호 재설정을 위한 임시 비밀번호를 생성합니다.
     * 
     * @param email 임시 비밀번호를 발급받을 사용자의 이메일 주소
     * @return 생성된 임시 비밀번호 문자열
     */
    String createTemporaryPassword(String email);

    /**
     * 사용자가 입력한 임시 비밀번호를 검증합니다.
     * 
     * @param email 비밀번호 재설정을 요청한 이메일 주소
     * @param tempPassword 사용자가 입력한 임시 비밀번호
     * @return 검증 성공 시 true, 실패 시 false
     */
    boolean verifyTemporaryPassword(String email, String tempPassword);

    /**
     * 임시 비밀번호를 포함한 이메일을 발송합니다.
     * 
     * @param email 임시 비밀번호를 받을 이메일 주소
     * @param tempPassword 발송할 임시 비밀번호
     */
    void sendTemporaryPasswordMail(String email, String tempPassword);
}