package com.example.lala.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class UserCommon {
    private String userId;              // UUID
    private String email;               // 이메일 (UNIQUE)
    private String nickname;            // 닉네임
    private String password;            // 비밀번호
    private Date passwordUpdate;        // 비밀번호 수정일 (nullable)
    private String profilename;         // 프로필 이름 (nullable)
    private String provider;            // 소셜 로그인 제공자 (nullable)
    private Date registrationDate;      // 가입일
    private int activeType;             // 활동 상태 (FK)
    private int userType;               // 사용자 유형 (FK)
    private Long roleId;                // 역할 ID (FK)
    private String socialLoginId;       // 소셜 로그인 ID (nullable)
    private String loginId;             // 로그인 ID (nullable)
}
