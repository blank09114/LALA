package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🎯 완료된 자료 기록 DTO
 * 
 * 📌 사용처:
 * - completed_materials 테이블 데이터 처리
 * - 진도율 계산 로직
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedMaterial {
    private String userId;
    private String lectureId;
    private String materialId;
    private String completedDate;
    
    // 🔍 유틸리티 메서드
    public boolean isCompletedToday() {
        // 오늘 완료된 자료인지 확인 (로직 구현 필요)
        return completedDate != null && 
               completedDate.startsWith(java.time.LocalDate.now().toString());
    }
}
