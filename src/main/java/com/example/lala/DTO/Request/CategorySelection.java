package com.example.lala.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자 관심사 카테고리 선택을 위한 DTO 클래스
 * 사용자 ID와 선택된 관심사 ID 목록을 담습니다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySelection {

    /**
     * 사용자 고유 식별자
     */
    private String userId;

    /**
     * 선택된 관심사 카테고리 ID 목록
     */
    private List<String> interestedId;
}