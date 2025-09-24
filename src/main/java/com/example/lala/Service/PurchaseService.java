package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Course.Lecture;
import com.example.lala.DTO.Course.PurchaseRequest;
import com.example.lala.DTO.Course.PurchaseResponse;
import com.example.lala.Mapper.LectureMapper;
import com.example.lala.Mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 🛒 강의 구매 관련 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService extends BaseService {

    private final PurchaseMapper purchaseMapper;
    private final LectureMapper lectureMapper;

    // ==================== 🔍 구매 전 검증 ====================
    
    /**
     * 🎯 구매 가능 여부 종합 체크
     * 
     * @param lectureId 강의 ID
     * @return 구매 가능 여부
     */
    public boolean canPurchaseLecture(String lectureId) {
        try {
            String buyerId = getCurrentUserId();
            
            // 1. 이미 구매했는지 확인
            if (purchaseMapper.isAlreadyPurchased(buyerId, lectureId)) {
                log.warn("[canPurchaseLecture] 이미 구매한 강의 - 사용자: {}, 강의: {}", buyerId, lectureId);
                return false;
            }
            
            // 2. 강의가 구매 가능한 상태인지 확인
            if (!purchaseMapper.isLectureAvailable(lectureId)) {
                log.warn("[canPurchaseLecture] 구매 불가능한 강의 - 강의: {}", lectureId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("[canPurchaseLecture] 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    // ==================== 💳 구매 처리 ====================
    
    /**
     * 🛒 강의 구매 처리 (핵심 메서드)
     * 
     * ✨ 처리 과정:
     * 1. 구매 가능 여부 검증
     * 2. 강의 정보 조회 및 가격 계산
     * 3. purchase 테이블에 구매 기록 저장
     * 4. 구매 완료 정보 반환
     * 
     * @param purchaseRequest 구매 요청 정보
     * @return 구매 완료 정보
     */
    @Transactional
    public PurchaseResponse processPurchase(PurchaseRequest purchaseRequest) {
        try {
            String buyerId = getCurrentUserId();
            String lectureId = purchaseRequest.getLectureId();
            
            log.info("[processPurchase] 구매 처리 시작 - 사용자: {}, 강의: {}, 결제방법: {}", 
                    buyerId, lectureId, purchaseRequest.getPurchaseType());
            
            // 1. 구매 가능 여부 재검증
            if (!canPurchaseLecture(lectureId)) {
                throw new RuntimeException("구매할 수 없는 강의입니다.");
            }
            
            // 2. 강의 정보 조회
            Lecture lectureInfo = lectureMapper.lectureDetail(lectureId);
            if (lectureInfo == null) {
                throw new RuntimeException("존재하지 않는 강의입니다.");
            }
            
            // 3. 가격 계산
            int lecturePrice = lectureInfo.getPrice();
            int discountRate = lectureInfo.getDiscountRate();
            int purchasePrice = calculatePurchasePrice(lecturePrice, discountRate);
            
            log.info("[processPurchase] 가격 계산 - 원가: {}원, 할인율: {}%, 최종가격: {}원", 
                    lecturePrice, discountRate, purchasePrice);
            
            // 4. 구매 정보 저장
            purchaseMapper.insertPurchase(
                buyerId,
                lectureId,
                lecturePrice,
                discountRate,
                purchasePrice,
                purchaseRequest.getPurchaseType()
            );
            
            // 5. 생성된 구매 ID 조회
            String purchaseId = purchaseMapper.getLastInsertedPurchaseId();
            
            // 6. 구매 완료 정보 반환
            PurchaseResponse response = purchaseMapper.getPurchaseById(purchaseId);
            
            log.info("[processPurchase] 구매 완료 - 구매ID: {}, 최종가격: {}원", purchaseId, purchasePrice);
            
            return response;
            
        } catch (RuntimeException e) {
            log.error("[processPurchase] 업무 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[processPurchase] 시스템 오류: {}", e.getMessage(), e);
            throw new RuntimeException("구매 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 💰 최종 구매 가격 계산
     * 
     * @param lecturePrice 강의 원가
     * @param discountRate 할인율 (%)
     * @return 최종 구매 가격
     */
    private int calculatePurchasePrice(int lecturePrice, int discountRate) {
        if (discountRate <= 0) {
            return lecturePrice;
        }
        
        // 할인 적용: 원가 * (100 - 할인율) / 100
        double discountedPrice = lecturePrice * (100.0 - discountRate) / 100.0;
        return (int) Math.round(discountedPrice);
    }

    // ==================== 📋 구매 내역 조회 ====================
    
    /**
     * 📄 사용자 구매 내역 조회
     * 
     * @return 구매 내역 목록
     */
    public List<PurchaseResponse> getUserPurchaseHistory() {
        try {
            String buyerId = getCurrentUserId();
            
            log.info("[getUserPurchaseHistory] 구매 내역 조회 - 사용자: {}", buyerId);
            
            List<PurchaseResponse> purchaseHistory = purchaseMapper.getUserPurchaseHistory(buyerId);
            
            log.info("[getUserPurchaseHistory] 성공 - 구매 건수: {}", purchaseHistory.size());
            
            return purchaseHistory;
            
        } catch (Exception e) {
            log.error("[getUserPurchaseHistory] 오류: {}", e.getMessage(), e);
            throw new RuntimeException("구매 내역 조회에 실패했습니다.", e);
        }
    }

    /**
     * 📊 최근 구매 내역 조회
     * 
     * @param limit 조회할 개수
     * @return 최근 구매 내역
     */
    public List<PurchaseResponse> getRecentPurchases(int limit) {
        try {
            String buyerId = getCurrentUserId();
            
            List<PurchaseResponse> recentPurchases = purchaseMapper.getRecentPurchases(buyerId, limit);
            
            log.info("[getRecentPurchases] 최근 구매 조회 - 사용자: {}, 건수: {}", buyerId, recentPurchases.size());
            
            return recentPurchases;
            
        } catch (Exception e) {
            log.error("[getRecentPurchases] 오류: {}", e.getMessage());
            throw new RuntimeException("최근 구매 내역 조회에 실패했습니다.", e);
        }
    }

    /**
     * 🔍 특정 구매 정보 조회
     * 
     * @param purchaseId 구매 ID
     * @return 구매 정보
     */
    public PurchaseResponse getPurchaseById(String purchaseId) {
        try {
            PurchaseResponse purchase = purchaseMapper.getPurchaseById(purchaseId);
            
            if (purchase == null) {
                throw new RuntimeException("존재하지 않는 구매 정보입니다.");
            }
            
            log.info("[getPurchaseById] 구매 정보 조회 성공 - 구매ID: {}", purchaseId);
            
            return purchase;
            
        } catch (Exception e) {
            log.error("[getPurchaseById] 오류: {}", e.getMessage());
            throw new RuntimeException("구매 정보 조회에 실패했습니다.", e);
        }
    }

    // ==================== 🔧 구매 관리 ====================
    
    /**
     * 🔄 환불 처리
     * 
     * @param purchaseId 구매 ID
     * @return 환불 성공 여부
     */
    @Transactional
    public boolean processRefund(String purchaseId) {
        try {
            log.info("[processRefund] 환불 처리 시작 - 구매ID: {}", purchaseId);
            
            // 1. 구매 정보 확인
            PurchaseResponse purchase = purchaseMapper.getPurchaseById(purchaseId);
            if (purchase == null) {
                throw new RuntimeException("존재하지 않는 구매 정보입니다.");
            }
            
            // 2. 이미 환불된 구매인지 확인
            if (purchase.isRefunded()) {
                throw new RuntimeException("이미 환불 처리된 구매입니다.");
            }
            
            // 3. 환불 처리
            purchaseMapper.processRefund(purchaseId);
            
            log.info("[processRefund] 환불 완료 - 구매ID: {}", purchaseId);
            
            return true;
            
        } catch (RuntimeException e) {
            log.error("[processRefund] 업무 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[processRefund] 시스템 오류: {}", e.getMessage(), e);
            throw new RuntimeException("환불 처리 중 오류가 발생했습니다.", e);
        }
    }
}
