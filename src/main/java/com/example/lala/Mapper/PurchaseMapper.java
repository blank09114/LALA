package com.example.lala.Mapper;

import com.example.lala.DTO.Course.PurchaseRequest;
import com.example.lala.DTO.Course.PurchaseResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 🛒 강의 구매 관련 Mapper
 */
@Mapper
public interface PurchaseMapper {

    // ==================== 🔍 구매 전 검증 ====================
    
    /**
     * 이미 구매한 강의인지 확인
     */
    boolean isAlreadyPurchased(@Param("buyerId") String buyerId, @Param("lectureId") String lectureId);
    
    /**
     * 강의가 구매 가능한 상태인지 확인 (활성화된 강의)
     */
    boolean isLectureAvailable(@Param("lectureId") String lectureId);

    // ==================== 💳 구매 처리 ====================
    
    /**
     * 구매 정보를 purchase 테이블에 삽입
     */
    void insertPurchase(@Param("buyerId") String buyerId,
                       @Param("lectureId") String lectureId,
                       @Param("lecturePrice") int lecturePrice,
                       @Param("discountRate") int discountRate,
                       @Param("purchasePrice") int purchasePrice,
                       @Param("purchaseType") String purchaseType);
    
    /**
     * 마지막 삽입된 구매 ID 조회
     */
    String getLastInsertedPurchaseId();

    // ==================== 📋 구매 내역 조회 ====================
    
    /**
     * 특정 구매 정보 조회
     */
    PurchaseResponse getPurchaseById(@Param("purchaseId") String purchaseId);
    
    /**
     * 사용자의 구매 내역 목록 조회
     */
    List<PurchaseResponse> getUserPurchaseHistory(@Param("buyerId") String buyerId);
    
    /**
     * 사용자의 최근 구매 내역 조회
     */
    List<PurchaseResponse> getRecentPurchases(@Param("buyerId") String buyerId, @Param("limit") int limit);

    // ==================== 🔧 구매 관리 ====================
    
    /**
     * 환불 처리
     */
    void processRefund(@Param("purchaseId") String purchaseId);
}
