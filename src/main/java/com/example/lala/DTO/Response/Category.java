package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카테고리 정보를 담는 DTO 클래스
 * 관심사 카테고리의 고유 ID와 카테고리명을 포함합니다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    
    /**
     * 관심사 카테고리의 고유 식별자
     */
    private String interestedCategoryId;
    
    /**
     * 카테고리명
     */
    private String categoryName;
}