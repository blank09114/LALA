package com.example.lala.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 재설정 요청 정보를 담는 DTO 클래스
 * 사용자가 입력한 이메일과 임시 비밀번호를 검증하기 위해 사용됩니다.
 */
@Getter
@Setter
public class PasswordResetRequest {

    /**
     * 비밀번호 재설정을 요청한 사용자의 이메일 주소
     */
    private String userEmailAddress;
    
    /**
     * 시스템에서 발급한 임시 비밀번호
     */
    private String temporaryPassword;
}