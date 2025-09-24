package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🛒 강의 구매 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    
    // 📚 강의 정보 (필수)
    private String lectureId;
    
    // 💳 결제 정보 (필수)
    private String purchaseType; // "kakaopay", "card", "bank" 등
    
    // 📝 기타 정보 (선택적)
    private String memo; // 구매 메모
}
