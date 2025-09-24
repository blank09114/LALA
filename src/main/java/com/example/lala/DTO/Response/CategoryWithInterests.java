package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카테고리와 하위 관심분야 정보를 담는 DTO
 * 강의 생성 시 카테고리 선택용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithInterests {
    
    /**
     * 카테고리 기본 정보
     */
    private Integer categoryId;      // category 테이블의 category_id
    private String categoryName;     // category 테이블의 name (예: "외국어", "개발IT")
    
    /**
     * 해당 카테고리의 하위 관심분야 목록
     */
    private List<InterestInfo> interests;
    
    /**
     * 관심분야 상세 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestInfo {
        private String interestedId;    // interested 테이블의 interested_id (예: "I010")
        private String interestedName;  // interested 테이블의 NAME (예: "영어", "웹개발")
        private Integer categoryId;     // 상위 카테고리 ID
        
        /**
         * 화면 표시용 아이콘 반환
         * 카테고리별로 적절한 이모지 아이콘 제공
         */
        public String getIcon() {
            if (interestedName == null) return "📚";
            
            // 외국어 카테고리
            if (interestedName.contains("영어")) return "🇺🇸";
            if (interestedName.contains("일본어")) return "🇯🇵";
            if (interestedName.contains("중국어")) return "🇨🇳";
            if (interestedName.contains("프랑스어")) return "🇫🇷";
            
            // 예술 카테고리
            if (interestedName.contains("미술")) return "🎨";
            if (interestedName.contains("음악")) return "🎵";
            if (interestedName.contains("사진")) return "📸";
            if (interestedName.contains("영상")) return "🎬";
            
            // 개발IT 카테고리
            if (interestedName.contains("웹개발")) return "🌐";
            if (interestedName.contains("앱개발")) return "📱";
            if (interestedName.contains("데이터")) return "📊";
            if (interestedName.contains("인공지능")) return "🤖";
            
            // 운동 카테고리
            if (interestedName.contains("요가") || interestedName.contains("필라테스")) return "🧘";
            if (interestedName.contains("재활")) return "🏥";
            if (interestedName.contains("피트니스")) return "💪";
            if (interestedName.contains("스포츠")) return "⚽";
            
            // 취미 카테고리
            if (interestedName.contains("요리")) return "👨‍🍳";
            if (interestedName.contains("뷰티")) return "💄";
            if (interestedName.contains("공예")) return "🎭";
            if (interestedName.contains("디저트")) return "🍰";
            
            // 비즈니스 카테고리
            if (interestedName.contains("창업")) return "🚀";
            if (interestedName.contains("경영")) return "💼";
            if (interestedName.contains("재무") || interestedName.contains("회계")) return "💰";
            if (interestedName.contains("마케팅")) return "📈";
            
            // 기타
            if (interestedName.contains("기타")) return "📋";
            
            return "📚"; // 기본 아이콘
        }
    }
    
    /**
     * 카테고리별 대표 아이콘 반환
     */
    public String getCategoryIcon() {
        if (categoryName == null) return "📚";
        
        switch (categoryName) {
            case "외국어": return "🌍";
            case "예술": return "🎨";
            case "개발IT": return "💻";
            case "운동": return "💪";
            case "취미": return "🎭";
            case "비즈니스": return "💼";
            default: return "📚";
        }
    }
    
    /**
     * 카테고리별 설명 반환
     */
    public String getCategoryDescription() {
        if (categoryName == null) return "";
        
        switch (categoryName) {
            case "외국어": return "다양한 언어 학습 강의";
            case "예술": return "창작과 표현을 위한 예술 강의";
            case "개발IT": return "프로그래밍 및 IT 기술 강의";
            case "운동": return "건강한 몸과 마음을 위한 운동 강의";
            case "취미": return "일상을 풍요롭게 하는 취미 강의";
            case "비즈니스": return "성공적인 비즈니스를 위한 실무 강의";
            default: return "다양한 주제의 학습 강의";
        }
    }
}
