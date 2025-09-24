package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강의 정보를 담는 DTO 클래스
 * 강의의 기본 정보와 최근 업데이트 정보를 포함합니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Lecture {
    
    /**
     * 강의 고유 식별자
     */
    private String lectureId;
    
    /**
     * 강의 제목
     */
    private String lectureTitle;
    
    /**
     * 강의 제공자(강사) 이름
     */
    private String provider;
    
    /**
     * 강의 썸네일 이미지 URL
     */
    private String thumbnailUrl;
    
    /**
     * 최근 업데이트 일시
     */
    private String lastUpdateDate;
}