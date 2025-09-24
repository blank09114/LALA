package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 담는 DTO 클래스
 * 사용자의 기본 프로필 정보를 전송하기 위해 사용됩니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserProfile {
    
    /**
     * 사용자 계정 유형 (일반, 관리자 등)
     */
    private int accountType;
    
    /**
     * 사용자 프로필 이미지 URL 주소
     */
    private String profileName;
    
    /**
     * 사용자 닉네임
     */
    private String userNickname;
    
    /**
     * 사용자 이메일 주소
     */
    private String emailAddress;

    private String passwordUpdate;
}