package com.example.lala.DTO.Lms;

import lombok.Data;

@Data
public class RegQuestion {
    private String questionId;    // 📝 문제 ID (DB에서 자동 생성)
    private String materialId;    // 🔗 연결될 자료 ID (추가 필요!)
    private String question;      // ❓ 문제 내용
    private String choiceAnswer;  // 📋 선택지
    private String type;          // 🏷️ 문제 타입
    private String answer;        // ✅ 정답
    private String comment;       // 💬 해설
}
