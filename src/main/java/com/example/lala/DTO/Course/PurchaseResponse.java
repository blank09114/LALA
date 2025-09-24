package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🛒 강의 구매 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    
    // 📋 구매 기본 정보
    private String purchaseId;
    private String lectureId;
    private String lectureName;
    private String buyerId;
    
    // 💰 결제 정보
    private int lecturePrice;    // 원가
    private int discountRate;    // 할인율
    private int purchasePrice;   // 실제 결제 금액
    private String purchaseType; // 결제 방법
    private String purchaseDate; // 구매 일시
    
    // 🎯 추가 정보
    private boolean refundCheck; // 환불 여부
    private String refundDate;   // 환불 일시
    
    // 🔍 유틸리티 메서드
    public String getFormattedPrice() {
        return String.format("%,d원", purchasePrice);
    }
    
    public boolean isRefunded() {
        return refundCheck;
    }
}
