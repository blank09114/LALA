package com.example.lala.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 요청 정보를 담는 DTO 클래스
 * 비밀번호 재설정, 이메일 인증 등의 메일 관련 요청에 사용됩니다.
 */
@Getter
@Setter
public class EmailRequest {

    /**
     * 요청에 포함된 이메일 주소
     */
    private String mail;
}