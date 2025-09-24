package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Request.ProReqRequest;
import com.example.lala.DTO.Response.*;
import com.example.lala.Mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MypageService extends BaseService {

    private final UserMapper userMapper;
    private final InterestMapper interestMapper;
    private final CalendarMapper calendarMapper;
    private final LectureMapper lectureMapper;
    private final CommunityMapper communityMapper;
    private final PlannerMapper plannerMapper;
    private final ReviewMapper reviewMapper;
    private final CategoryAllMapper categoryAllMapper;
    private final InformationProviderMapper informationProviderMapper;
    private final FileUploadService fileUploadService;

    // 병렬 처리를 위한 스레드 풀
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    public Map<String, Object> getMypageData(String userId) {
        try {
            // 병렬로 데이터 조회
            CompletableFuture<UserProfile> userFuture =
                    CompletableFuture.supplyAsync(() -> getUserSafely(userId), executorService);

            CompletableFuture<List<UserInterest>> interestFuture =
                    CompletableFuture.supplyAsync(() -> interestMapper.selectUserInterested(userId), executorService);

            CompletableFuture<List<CalendarSchedule>> calendarFuture =
                    CompletableFuture.supplyAsync(() -> calendarMapper.selectByUserId(userId), executorService);

            CompletableFuture<List<Planner>> plannerFuture =
                    CompletableFuture.supplyAsync(() -> plannerMapper.selectThisWeek(userId), executorService);

            CompletableFuture<MyLecture> lectureFuture =
                    CompletableFuture.supplyAsync(() -> lectureMapper.selectLastUpdate(userId), executorService);

            CompletableFuture<List<CommunityPost>> communityFuture =
                    CompletableFuture.supplyAsync(() -> communityMapper.selectLastComm(userId), executorService);

            // 모든 작업 완료 대기
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    userFuture, interestFuture, calendarFuture,
                    plannerFuture, lectureFuture, communityFuture
            );

            return allFutures.thenApply(v -> {
                Map<String, Object> data = new HashMap<>();
                data.put("User", userFuture.join());
                data.put("Interest", interestFuture.join());
                data.put("Calendar", calendarFuture.join());
                data.put("Planner", plannerFuture.join());
                data.put("Lecture", lectureFuture.join());
                data.put("Community", communityFuture.join());
                return data;
            }).get();

        } catch (Exception e) {
            log.error("마이페이지 데이터 로딩 실패 - 사용자: {}", userId, e);
            throw new RuntimeException("마이페이지 데이터 로딩 중 오류가 발생했습니다.", e);
        }
    }

    public List<Review> getMyReviews(String userId) {
        try {
            log.debug("사용자 리뷰 조회 시작 - userId: {}", userId);
            List<Review> reviews = reviewMapper.selectMyReview(userId);
            log.debug("사용자 리뷰 조회 완료 - userId: {}, 리뷰 수: {}", userId, reviews.size());
            return reviews;
        } catch (Exception e) {
            log.error("사용자 리뷰 조회 실패 - userId: {}", userId, e);
            throw new RuntimeException("리뷰 데이터를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    public List<Review> getMyReviews() {
        return getMyReviews(getCurrentUserId());
    }

    public UserProfile getUserSafely(String userId) {
        UserProfile user = userMapper.selectById(userId);

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }

        return user;
    }

    public Map<String, Object> getMypageData() {
        Map<String, Object> data = getMypageData(getCurrentUserId());
        log.debug(getCurrentUserId());
        return data;
    }

    public Map<String, Object> getMypageUserData() {
        Map<String, Object> data = new HashMap<>();
        data.put("User", userMapper.selectById(getCurrentUserId()));
        data.put("Interest", interestMapper.selectUserInterested(getCurrentUserId()));
        return data;
    }

    public UserProfile getCurrentUserProfile() {
        return getUserSafely(getCurrentUserId());
    }

    @Transactional
    public int updateReview(Map<String, Object> prams) {
        int result;
        try {
            result = reviewMapper.updateReview(prams);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 수정 중 오류가 발생했습니다.", e);
        }
        return result;
    }

    @Transactional
    public int insertReview(Map<String, Object> prams) {
        prams.put("userId", getCurrentUserId());
        int result;
        try {
            System.out.println(prams);
            result = reviewMapper.insertReview(prams);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 수정 중 오류가 발생했습니다.", e);
        }
        return result;
    }

    @Transactional
    public MyReview checkReview(String lectureId) {
        Map<String, Object> prams = new HashMap<>();
        prams.put("lectureId", lectureId);
        prams.put("userId", getCurrentUserId());
        MyReview result;
        try {
            System.out.println(prams);
            result = reviewMapper.checkReview(prams);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 수정 중 오류가 발생했습니다.", e);
        }
        return result;
    }

    public List<MyLearning> getUserLecture(int filterType) {
        String userId = getCurrentUserId();
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId); // String으로 명시적 변환
        params.put("filterType", String.valueOf(filterType)); // Integer를 String으로 변환

        return lectureMapper.selectUserLearning(params);
    }

    public List<CancelLecture> getCancelLecture(int page, int size) {
        int offset = (page - 1) * size;
        Map<String, Object> params = new HashMap<>();
        params.put("userId", getCurrentUserId());
        params.put("offset", offset);
        params.put("size", size);
        List<CancelLecture> data = lectureMapper.selectCancelLectures(params);
        return data;
    }

    public int getCancelLectureCount() {
        return lectureMapper.selectCancelLectureCount(getCurrentUserId());
    }

    // 기존 메소드 유지
    public List<LikeProvider> getLikeProvider() {
        return informationProviderMapper.selectUserLikes(getCurrentUserId());
    }

    // 페이지네이션을 지원하는 새로운 메소드
    public List<LikeProvider> getLikeProvider(int page, int size) {
        String userId = getCurrentUserId();
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("offset", (page - 1) * size);
        params.put("limit", size);
        return informationProviderMapper.selectUserLikesWithPagination(params);
    }

    /*찜관련*/
    public boolean toggleLikeProvider(String providerId) {
        String userId = getCurrentUserId();

        LikeProvider existing = informationProviderMapper.selectOneUserLike(userId, providerId);
        if (existing != null) {
            informationProviderMapper.deleteUserLike(userId, providerId);
            return false; // 찜 해제
        } else {
            informationProviderMapper.insertUserLike(userId, providerId);
            return true; // 찜 추가
        }
    }




    public InformationProvider selectInformationProvider() {
        return informationProviderMapper.selectInformationProvider(getCurrentUserId());
    }

    public int updateExternalLink(String link) {
        return informationProviderMapper.updateExternalLink(getCurrentUserId(), link);
    }

    public int updateIntroduction(String introduction) {
        return informationProviderMapper.updateIntroduction(getCurrentUserId(), introduction);
    }

    public int insertProReq(String name, String nickname, String intro, String externallink) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", getCurrentUserId());
        params.put("name", name);
        params.put("nickname", nickname);
        params.put("intro", intro);
        params.put("externallink", externallink);
        return informationProviderMapper.insertProReq(params);
    }

    /**
     * 학력/약력과 첨부파일을 포함한 지식 제공자 신청을 처리합니다.
     *
     * @param name         실명
     * @param nickname     활동명
     * @param intro        소개
     * @param externallink 외부 링크
     * @param historyItems 학력/약력 항목 리스트
     * @return Map<String, Object> 처리 결과와 학력/약력 데이터
     */
    @Transactional
    public Map<String, Object> insertProReqWithHistory(String name, String nickname, String intro, String externallink,
                                                       List<ProReqRequest.HistoryItem> historyItems) {
        try {
            String userId = getCurrentUserId();
            log.info("지식 제공자 신청 처리 시작 - 사용자 ID: {}", userId);

            // 1. 기본 신청 정보 저장
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("name", name);
            params.put("nickname", nickname);
            params.put("intro", intro);
            params.put("externallink", externallink);
            int result = informationProviderMapper.insertProReq(params);

            if (result == 0) {
                log.error("기본 신청 정보 저장 실패 - 사용자 ID: {}", userId);
                throw new RuntimeException("기본 신청 정보 저장에 실패했습니다.");
            }

            // 2. 학력/약력 데이터를 supporting_material 테이블에 저장
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "지식 제공자 신청이 성공적으로 제출되었습니다.");
            response.put("userId", userId);

            if (historyItems != null && !historyItems.isEmpty()) {
                List<Map<String, Object>> historyData = new ArrayList<>();

                for (int i = 0; i < historyItems.size(); i++) {
                    ProReqRequest.HistoryItem item = historyItems.get(i);
                    Map<String, Object> historyItem = new HashMap<>();

                    // 학력/약력 텍스트
                    historyItem.put("text", item.getText());
                    historyItem.put("index", i);

                    // 첨부파일이 있는 경우 S3에 업로드
                    String fileUrl = null;
                    if (item.getFile() != null && !item.getFile().isEmpty()) {
                        try {
                            fileUrl = fileUploadService.saveHistoryAttachment(item.getFile(), userId, i);
                            historyItem.put("fileUrl", fileUrl);
                            log.info("첨부파일 업로드 완료 - 인덱스: {}, URL: {}", i, fileUrl);
                        } catch (IOException e) {
                            log.error("첨부파일 업로드 실패 - 인덱스: {}, 오류: {}", i, e.getMessage(), e);
                            throw new RuntimeException("첨부파일 업로드에 실패했습니다: " + e.getMessage(), e);
                        }
                    }

                    // supporting_material 테이블에 저장
                    Map<String, Object> dbParams = new HashMap<>();
                    dbParams.put("promotionRequestId", userId); // user_id로 사용
                    dbParams.put("text", item.getText()); // career로 저장
                    dbParams.put("fileUrl", fileUrl); // fileURL로 저장

                    int dbResult = informationProviderMapper.insertProReqHistory(dbParams);

                    if (dbResult == 0) {
                        log.error("학력/약력 정보 DB 저장 실패 - 인덱스: {}", i);
                        throw new RuntimeException("학력/약력 정보 DB 저장에 실패했습니다.");
                    }

                    historyData.add(historyItem);
                    log.info("학력/약력 정보 DB 저장 완료 - 인덱스: {}, career: {}", i, item.getText());
                }

                response.put("historyData", historyData);
                log.info("학력/약력 데이터 저장 완료 - 항목 수: {}", historyData.size());
            }

            log.info("지식 제공자 신청 처리 완료 - 사용자 ID: {}", userId);

            int updateResult = userMapper.updateUserType(userId, "2");
            return response;

        } catch (Exception e) {
            log.error("지식 제공자 신청 처리 실패 - 오류: {}", e.getMessage(), e);
            throw new RuntimeException("지식 제공자 신청 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자의 학력/약력 정보를 조회합니다.
     *
     * @return List<Map < String, Object>> 학력/약력 정보 리스트
     */
    public List<Map<String, Object>> getProReqHistory() {
        try {
            String userId = getCurrentUserId();
            log.info("학력/약력 정보 조회 시작 - 사용자 ID: {}", userId);

            List<Map<String, Object>> historyList = informationProviderMapper.selectProReqHistory(userId);

            log.info("학력/약력 정보 조회 완료 - 항목 수: {}", historyList.size());
            return historyList;

        } catch (Exception e) {
            log.error("학력/약력 정보 조회 실패 - 오류: {}", e.getMessage(), e);
            throw new RuntimeException("학력/약력 정보 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public int userTypeUpdate() {
        String userId = getCurrentUserId();
        return userMapper.updateUserType(userId, "2");

    }

    public List<IPLecture> getIPLecture(boolean free) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", getCurrentUserId());
        params.put("free", free);
        return lectureMapper.selectIPLecture(params);
    }

    /**
     * 페이지네이션과 함께 강의 제공자의 강의 목록을 조회합니다.
     *
     * @param free 무료 강의 여부
     * @param offset 조회 시작 위치
     * @param size 조회할 강의 수
     * @return 강의 목록
     */
    public List<IPLecture> getIPLectureWithPagination(boolean free, int offset, int size) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("userId", currentUserId);
            params.put("free", free);
            params.put("offset", offset);
            params.put("size", size);
            
            return lectureMapper.selectIPLectureWithPagination(params);
            
        } catch (Exception e) {
            log.error("페이지네이션 강의 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("강의 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 강의 제공자의 전체 강의 수를 조회합니다.
     *
     * @param free 무료 강의 여부
     * @return 전체 강의 수
     */
    public int getIPLectureCount(boolean free) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("userId", currentUserId);
            params.put("free", free);
            
            return lectureMapper.selectIPLectureCount(params);
            
        } catch (Exception e) {
            log.error("전체 강의 수 조회 실패: {}", e.getMessage());
            throw new RuntimeException("강의 수 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 장바구니에서 선택된 강의들을 구매 처리합니다.
     *
     * @param lectureIds 구매할 강의 ID 리스트
     * @return 구매 처리 결과
     */
    @Transactional
    public Map<String, Object> purchaseLecture(List<String> lectureIds) {
        try {
            String userId = getCurrentUserId();
            log.info("강의 구매 처리 시작 - 사용자 ID: {}, 강의 수: {}", userId, lectureIds.size());

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> purchaseResults = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (String lectureId : lectureIds) {
                try {
                    log.info("강의 구매 처리 시작 - 강의 ID: {}", lectureId);
                    
                    // 강의 정보 조회
                    Map<String, Object> lectureInfo = lectureMapper.selectLectureInfo(lectureId);
                    if (lectureInfo == null) {
                        log.warn("강의 정보를 찾을 수 없음 - 강의 ID: {}", lectureId);
                        failCount++;
                        continue;
                    }
                    log.info("강의 정보 조회 성공 - 강의 ID: {}, 가격: {}, 할인율: {}", 
                        lectureId, lectureInfo.get("price"), lectureInfo.get("discountRate"));

                    // 이미 구매한 강의인지 확인
                    Map<String, Object> checkParams = new HashMap<>();
                    checkParams.put("userId", userId);
                    checkParams.put("lectureId", lectureId);
                    boolean alreadyPurchased = lectureMapper.checkAlreadyPurchased(checkParams);
                    if (alreadyPurchased) {
                        log.warn("이미 구매한 강의 - 사용자 ID: {}, 강의 ID: {}", userId, lectureId);
                        failCount++;
                        continue;
                    }
                    log.info("구매 가능한 강의 확인 - 강의 ID: {}", lectureId);

                    // 할인된 가격 계산
                    Number priceNumber = (Number) lectureInfo.get("price");
                    Number discountNumber = (Number) lectureInfo.get("discountRate");
                    int originalPrice = priceNumber.intValue();
                    int discountRate = discountNumber.intValue();
                    int purchasePrice = originalPrice - (originalPrice * discountRate / 100);

                    // 구매 정보 저장
                    Map<String, Object> purchaseParams = new HashMap<>();
                    purchaseParams.put("lectureId", lectureId);
                    purchaseParams.put("buyerId", userId);
                    purchaseParams.put("lecturePrice", originalPrice);
                    purchaseParams.put("discountRate", discountRate);
                    purchaseParams.put("purchasePrice", purchasePrice);
                    purchaseParams.put("purchaseType", "CART");

                    log.info("구매 파라미터 준비 완료 - 강의 ID: {}, 원가: {}, 할인율: {}, 구매가: {}", 
                        lectureId, originalPrice, discountRate, purchasePrice);

                    int purchaseResult = lectureMapper.purchaseLecture(purchaseParams);
                    log.info("구매 테이블 저장 결과 - 강의 ID: {}, 결과: {}", lectureId, purchaseResult);
                    
                    if (purchaseResult > 0) {
                        // user_lecture 테이블에 수강 정보 추가
                        Map<String, Object> userLectureParams = new HashMap<>();
                        userLectureParams.put("userId", userId);
                        userLectureParams.put("lectureId", lectureId);
                        
                        int userLectureResult = lectureMapper.insertUserLecture(userLectureParams);
                        log.info("수강 정보 저장 결과 - 강의 ID: {}, 결과: {}", lectureId, userLectureResult);

                        successCount++;
                        purchaseResults.add(new HashMap<String, Object>() {{
                            put("lectureId", lectureId);
                            put("status", "success");
                            put("message", "구매 완료");
                        }});
                        
                        log.info("강의 구매 성공 - 사용자 ID: {}, 강의 ID: {}", userId, lectureId);
                    } else {
                        log.error("구매 테이블 저장 실패 - 강의 ID: {}", lectureId);
                        failCount++;
                        purchaseResults.add(new HashMap<String, Object>() {{
                            put("lectureId", lectureId);
                            put("status", "fail");
                            put("message", "구매 처리 실패");
                        }});
                    }

                } catch (Exception e) {
                    log.error("강의 구매 처리 중 오류 - 강의 ID: {}, 오류: {}", lectureId, e.getMessage(), e);
                    failCount++;
                    purchaseResults.add(new HashMap<String, Object>() {{
                        put("lectureId", lectureId);
                        put("status", "fail");
                        put("message", "처리 중 오류 발생: " + e.getMessage());
                    }});
                }
            }

            result.put("success", successCount > 0);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("purchaseResults", purchaseResults);
            result.put("message", String.format("총 %d개 중 %d개 구매 완료", lectureIds.size(), successCount));

            log.info("강의 구매 처리 완료 - 성공: {}, 실패: {}", successCount, failCount);
            return result;

        } catch (Exception e) {
            log.error("강의 구매 처리 실패 - 오류: {}", e.getMessage(), e);
            throw new RuntimeException("강의 구매 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }
}