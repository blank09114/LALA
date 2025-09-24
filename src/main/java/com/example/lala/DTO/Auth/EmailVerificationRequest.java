package com.example.lala.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 인증 요청 정보를 담는 DTO 클래스
 * 사용자가 입력한 이메일과 인증코드를 검증하기 위해 사용됩니다.
 */
@Getter
@Setter
public class EmailVerificationRequest {

    /**
     * 인증을 요청한 이메일 주소
     */
    private String emailAddress;
    
    /**
     * 사용자가 입력한 인증 코드
     */
    private int verificationCode;
}