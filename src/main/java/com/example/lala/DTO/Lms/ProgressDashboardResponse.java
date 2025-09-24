package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📈 수강률 대시보드 응답 DTO
 * 
 * 📌 사용처:
 * - 지식제공자용 수강률 대시보드
 * - 관리자용 강의 현황 조회
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDashboardResponse {
    
    // 📋 기본 강의 정보
    private String lectureId;
    private String lectureName;
    private String instructorName;
    private int totalStudents;
    private int activeStudents;
    
    // 📊 수강률 통계
    private double averageProgress;
    private int completedStudents;
    private int inProgressStudents;
    private int notStartedStudents;
    
    // 📈 진도율 분포
    private List<ProgressDistribution> progressDistribution;
    
    // 🎯 최근 활동
    private List<RecentActivity> recentActivities;
    
    /**
     * 📊 진도율 분포 내부 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressDistribution {
        private String range; // "0-20%", "21-40%", etc.
        private int studentCount;
        private double percentage;
    }
    
    /**
     * 🎯 최근 활동 내부 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String studentName;
        private String activityType; // "material_completed", "chapter_finished"
        private String materialName;
        private String timestamp;
        private double newProgressRate;
    }
    
    // 🔍 유틸리티 메서드
    public double getCompletionRate() {
        return totalStudents > 0 ? (double) completedStudents / totalStudents * 100 : 0.0;
    }
    
    public boolean hasActiveStudents() {
        return activeStudents > 0;
    }
    
    public String getEngagementLevel() {
        if (averageProgress >= 80) return "매우 높음";
        if (averageProgress >= 60) return "높음";
        if (averageProgress >= 40) return "보통";
        if (averageProgress >= 20) return "낮음";
        return "매우 낮음";
    }
}
