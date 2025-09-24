package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🎯 강의 등록 요청 DTO
 * 기존 RegLecture + interestedId를 포함
 * 
 * 이 DTO는 JSON body로 받는 강의 등록 요청을 위해 사용됩니다.
 * 외래키 제약조건 문제를 해결하기 위해 interestedId를 함께 받습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRegistrationRequest {
    
    // 🎯 기본 강의 정보
    private String name;                    // 강의 제목
    private String outline;                 // 강의 개요
    private String introduction;            // 강의 소개글
    private String thumbnail;               // 썸네일 이미지 URL
    private Integer price;                  // 정가
    private Integer discountRate;           // 할인율
    private Integer difficulty;             // 난이도 (1:초급, 2:중급, 3:고급)
    
    // 🎯 카테고리 정보 (핵심 추가 사항)
    private String interestedId;            // 관심분야 ID (I010, I011, ...)
    
    /**
     * 이 DTO를 RegLecture로 변환하는 메서드
     * uploaderId는 서비스에서 별도로 설정됩니다.
     */
    public RegLecture toRegLecture() {
        return RegLecture.builder()
                .name(this.name)
                .outline(this.outline)
                .introduction(this.introduction)
                .thumbnail(this.thumbnail)
                .price(this.price)
                .discountRate(this.discountRate)
                .difficulty(this.difficulty)
                // uploaderId는 서비스에서 설정
                .build();
    }
    
    /**
     * LectureField 객체를 생성하는 메서드
     * lectureId는 강의 등록 후 설정됩니다.
     */
    public LectureField toLectureField() {
        return LectureField.builder()
                .interestedId(this.interestedId)
                // lectureId는 강의 등록 후 설정
                .build();
    }
}
