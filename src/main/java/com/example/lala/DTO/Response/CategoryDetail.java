package com.example.lala.DTO.Response;

import lombok.Data;

/**
 * 단일 카테고리와 관심사 매핑 정보를 담는 DTO 클래스
 * 카테고리명과 해당 카테고리의 관심사명을 포함합니다.
 */
@Data
public class CategoryDetail {

    /**
     * 카테고리의 이름
     */
    private String categoryName;
    
    /**
     * 관심사(세부 카테고리)의 이름
     */
    private String interestedName;
    
    /**
     * 관심사 ID
     */
    private String interestedCategoryId;
}


