package com.example.lala.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * OAuth2 로그인 시 제공업체 불일치 예외 클래스
 * 
 * 사용자가 이미 다른 제공업체(구글, 카카오 등)로 가입된 이메일로
 * 다른 제공업체를 통해 로그인을 시도할 때 발생하는 예외입니다.
 * 
 * 예시 상황:
 * - 구글로 가입한 사용자가 같은 이메일로 카카오 로그인 시도
 * - 일반 회원가입한 사용자가 소셜 로그인 시도
 * - 카카오로 가입한 사용자가 구글 로그인 시도
 * 
 * Spring Security의 AuthenticationException을 상속하여
 * 인증 과정에서의 예외로 처리됩니다.
 */
public class ProviderMismatchException extends AuthenticationException {

    /**
     * 제공업체 불일치 예외를 생성합니다.
     * 
     * @param message 예외 상세 메시지
     */
    public ProviderMismatchException(String message) {
        super(message);
    }
}