package com.example.lala.DTO.Request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 강의 필터링 요청을 위한 DTO 클래스 (더보기 방식)
 * 메인 페이지에서 다양한 조건으로 강의를 필터링할 때 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureFilterRequest {
    
    // 카테고리 필터 (IT, 비즈니스, 예술 등)
    private String categoryId;
    
    // 세부 관심분야 필터
    private String interestedId;
    
    // 검색 키워드
    private String keyword;
    
    // 가격 타입 필터 ("free", "paid", "discount", "all")
    private String priceType;
    
    // 난이도 필터 ("초급", "중급", "고급")
    private String difficulty;
    
    // 정렬 기준 ("latest", "rating", "bookmark", "price_low", "price_high")
    private String orderBy;
    
    // 최소 평점 (선택사항)
    private Double minRating;
    
    // 더보기 방식 처리
    private Integer offset;     // 이미 로드된 강의 개수
    private Integer limit;      // 한 번에 로드할 강의 개수 (기본 12개)
    
    // 기본값 설정을 위한 빌더 후처리
    public static class LectureFilterRequestBuilder {
        public LectureFilterRequest build() {
            // 기본값 설정
            if (this.orderBy == null || this.orderBy.isEmpty()) {
                this.orderBy = "latest";
            }
            if (this.priceType == null || this.priceType.isEmpty()) {
                this.priceType = "all";
            }
            if (this.offset == null || this.offset < 0) {
                this.offset = 0; // 처음 로드시 0
            }
            if (this.limit == null || this.limit < 1) {
                this.limit = 12; // 기본 12개씩
            }
            
            return new LectureFilterRequest(
                this.categoryId, this.interestedId, this.keyword, this.priceType, 
                this.difficulty, this.orderBy, this.minRating,
                this.offset, this.limit
            );
        }
    }
    
    /**
     * DTO를 MyBatis에서 사용할 Map으로 변환
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        
        // null이 아닌 값만 Map에 추가
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            map.put("categoryId", categoryId);
        }
        if (interestedId != null && !interestedId.trim().isEmpty()) {
            map.put("interestedId", interestedId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            map.put("keyword", keyword.trim());
        }
        if (priceType != null && !priceType.trim().isEmpty() && !"all".equals(priceType)) {
            map.put("priceType", priceType);
        }
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            map.put("difficulty", difficulty);
        }
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            map.put("orderBy", orderBy);
        }
        if (minRating != null && minRating > 0) {
            map.put("minRating", minRating);
        }
        if (offset != null && offset >= 0) {
            map.put("offset", offset);
        }
        if (limit != null && limit > 0) {
            map.put("limit", limit);
        }
        
        return map;
    }
}
