package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 캘린더 일정 정보를 담는 DTO 클래스
 * 특정 날짜와 해당 날짜의 일정 내용을 포함합니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CalendarSchedule {
    
    /**
     * 캘린더 일정 날짜
     */
    private String calendarDate;
    
    /**
     * 일정 내용
     */
    private String content;
}