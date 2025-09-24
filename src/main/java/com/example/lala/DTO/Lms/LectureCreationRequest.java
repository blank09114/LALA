package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 🚀 전체 강의 생성을 위한 통합 요청 DTO
 * 강의 등록 → 목차 등록 → 자료 등록을 한 번에 처리하기 위한 데이터 클래스
 * 🆕 다중 카테고리 지원 추가
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCreationRequest {
    
    // 📚 강의 기본 정보
    private RegLecture lectureInfo;
    
    // 🔗 강의 관심분야 ID (단일 - 기존 호환성 유지)
    private String interestedId;
    
    // 🆕 🔗 강의 관심분야 ID 리스트 (다중 - 최대 2개)
    private List<String> interestedIds;
    
    // 📖 목차 및 자료 정보 리스트
    private List<ChapterWithMaterials> chapters;
    
    /**
     * 🔧 interestedIds getter 메서드
     * - interestedIds가 있으면 그것을 반환
     * - 없으면 interestedId를 리스트로 변환하여 반환
     */
    public List<String> getInterestedIds() {
        // 1. interestedIds 리스트가 있고 비어있지 않으면 반환
        if (this.interestedIds != null && !this.interestedIds.isEmpty()) {
            return this.interestedIds;
        }
        
        // 2. interestedId 단일값이 있으면 리스트로 변환하여 반환
        if (this.interestedId != null && !this.interestedId.trim().isEmpty()) {
            return List.of(this.interestedId);
        }
        
        // 3. 둘 다 없으면 빈 리스트 반환
        return List.of();
    }
    
    /**
     * 🔧 첫 번째 카테고리 ID 반환 (기존 호환성 유지)
     */
    public String getInterestedId() {
        List<String> ids = getInterestedIds();
        return ids.isEmpty() ? null : ids.get(0);
    }
    
    /**
     * 🔧 카테고리 유효성 검증
     */
    public boolean hasValidCategories() {
        List<String> ids = getInterestedIds();
        return !ids.isEmpty() && ids.size() <= 2 &&
               ids.stream().allMatch(id -> 
                   id != null && 
                   !id.trim().isEmpty() && 
                   id.matches("^I\\d{3}$")
               );
    }
    
    /**
     * 🔧 카테고리 개수 반환
     */
    public int getCategoryCount() {
        return getInterestedIds().size();
    }
    
    /**
     * 목차와 해당 목차의 자료들을 묶어서 관리하는 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterWithMaterials {
        
        // 📋 목차 정보
        private RegChapter chapterInfo;
        
        // 📁 해당 목차에 속하는 자료들
        private List<MaterialWithContent> materials;
    }
    
    /**
     * 자료와 해당 자료의 내용을 묶어서 관리하는 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialWithContent {
        
        // 🗂️ 자료 타입 (1: 영상, 2: 추가자료, 3: 문제)
        private int materialType;
        
        // 📄 자료 내용 정보
        private RegContent contentInfo;
        
        // 🎥 영상 자료 (materialType이 1일 때만 사용)
        private RegVideoMaterial videoMaterial;
        
        // 📎 추가 자료 (materialType이 2일 때만 사용)
        private RegAddiMaterial addiMaterial;
        
        // ❓ 문제 자료 (materialType이 3일 때만 사용)
        private RegQuestionMaterial questionMaterial;
        
        // 📝 문제 내용 (materialType이 3일 때만 사용)
        private RegQuestion questionDetail;
    }
}