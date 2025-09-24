package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 관심사 정보를 담는 DTO 클래스
 * 카테고리와 해당 카테고리 내의 세부 관심사 정보를 포함합니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInterest {
    
    /**
     * 관심사가 속한 카테고리명
     */
    private String categoryName;
    
    /**
     * 세부 관심사명
     */
    private String interestedName;
}