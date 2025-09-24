package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지식제공자 요청 정보를 담는 DTO
 * 사용자 정보와 요청 내용을 함께 조회할 때 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProviderRequest {

    /**
     * 사용자 고유 식별자
     */
    private String userId;

    /**
     * 사용자 이메일
     */
    private String email;

    /**
     * 법적 이름 (실명)
     */
    private String legalName;

    /**
     * 닉네임
     */
    private String nickname;

    /**
     * 활동 유형 번호
     */
    private Integer activeType;

    /**
     * 활동 유형명
     */
    private String activeTypeName;

    /**
     * 사용자 유형 번호
     */
    private Integer userType;

    /**
     * 사용자 유형명
     */
    private String userTypeName;

    /**
     * 소개 내용
     */
    private String infoContent;

    /**
     * 외부 링크
     */
    private String externalLink;

    /**
     * 요청 일시
     */
    private LocalDateTime requestDate;

    /**
     * 승인 일시
     */
    private LocalDateTime approvalDate;
}