package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📊 수강률 업데이트 요청 DTO
 * 
 * 📌 사용처:
 * - 자료 클릭 시 진도율 업데이트
 * - 강의 완료 처리
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateRequest {
    private String lectureId;
    private String materialId;
    private String actionType; // "material_click", "chapter_complete", etc.
    
    // 🔍 유틸리티 메서드
    public boolean isMaterialClick() {
        return "material_click".equals(actionType);
    }
    
    public boolean isChapterComplete() {
        return "chapter_complete".equals(actionType);
    }
}
