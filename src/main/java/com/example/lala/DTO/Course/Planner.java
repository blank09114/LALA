package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 플래너 정보를 담는 DTO 클래스
 * 사용자의 일정 및 할 일 항목을 나타냅니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Planner {
    
    /**
     * 플래너 항목의 시작 날짜
     */
    private String startDate;
    
    /**
     * 플래너 항목의 종료 날짜
     */
    private String endDate;
    
    /**
     * 플래너 항목의 상세 내용
     */
    private String content;
    
    /**
     * 플래너 항목의 완료 여부
     */
    private boolean isCompleted;
}