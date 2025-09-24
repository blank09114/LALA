package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LectureProgressInfo {
    private String lectureId;
    private String lectureName;
    private double progressRate;
    private String lastUpdateDate;
    private int lectureStatus;
    private String uploaderType;

    // 🎯 유틸리티 메서드 (UI 분기용)
    public boolean isProgressStarted() {
        return progressRate > 0;
    }

    public boolean isInstructor() {
        return "instructor".equals(uploaderType);
    }

    public boolean isStudent() {
        return "student".equals(uploaderType);
    }

    public boolean isLectureRequested() {
        return lectureStatus == 2; // 승인 요청 중
    }
}
