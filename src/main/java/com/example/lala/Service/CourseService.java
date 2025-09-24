package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Course.*;
import com.example.lala.DTO.Request.LectureFilterRequest;
import com.example.lala.DTO.Response.MyLecture;
import com.example.lala.DTO.Response.CalendarSchedule;
import com.example.lala.DTO.Response.Planner;
import com.example.lala.DTO.Response.UserProfile;
import com.example.lala.Mapper.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService extends BaseService {

    private final CalendarMapper calendarMapper;
    private final PlannerMapper plannerMapper;
    private final UserMapper userMapper;
    private final LectureMapper lectureMapper;
    private final InformationProviderMapper informationProviderMapper;
    private final ReviewMapper reviewMapper;

    // 로그인한 아이디 갖고오기
    public String getUserId() {
        String userId;
        try {
            userId = getCurrentUserId();
            System.out.println("userId 불러오기");
            System.out.println(userId);
        } catch (RuntimeException e) {
            System.err.println("사용자 인증 실패: " + e.getMessage());
            throw e;
        }
        return userId;
    }

    // 플래너, 캘린더 정보
    public Map<String, Object> getStudyPlanner(String userId){

        // 플래너, 캘린더 정보 담을 map 구성
        Map<String, Object> datas = new HashMap<>();

        //데이터 수집
        List<CalendarSchedule> calendarList =  calendarMapper.selectByUserId(userId);
        List<Planner> plannerList = plannerMapper.selectThisWeek(userId);

        // 데이터 삽입
        datas.put("calender",calendarList);
        datas.put("planner",plannerList);

        return datas;
    }

    // 로그인한 유저의 정보
    public UserProfile getUserSafely(String userId) {
        UserProfile user = userMapper.selectById(userId);

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }

        return user;
    }

    // 유저가 마지막으로 들은 강의
    public MyLecture selectLastUpdate(String userId) {
        // lecture 또는 null을 반환하고 컨트롤러에서 예외처리
        return lectureMapper.selectLastUpdate(userId);
    }

    // 유저가 마지막으로 들은 강의
    public LastLecture recentLecture(String userId){
        return lectureMapper.recentLecture(userId);
    }

//    // 유저가 마지막으로 들은 강의
//    public MyLecture selectLastUpdate(String userId) {
//        // lecture 또는 null을 반환하고 컨트롤러에서 예외처리
//        return lectureMapper.selectLastUpdate(userId);
//    }

    // 전체 강의 조회
    public List<LectureSummary> allLectures(){
        return lectureMapper.allLectures();
    }

    // 새로 추가: 필터링된 강의 조회 (더보기 방식)
    public Map<String, Object> getFilteredLectures(LectureFilterRequest filterRequest) {
        log.info("[getFilteredLectures] 필터링 조건: {}", filterRequest);
        
        // 필터링 조건을 Map으로 변환
        Map<String, Object> params = filterRequest.toMap();
        
        // 강의 목록 조회
        List<LectureSummary> lectures = lectureMapper.getFilteredLectures(params);
        
        // 더보기 버튼 표시 여부 계산 (요청한 limit만큼 결과가 나왔으면 더 있을 가능성)
        boolean hasMore = lectures.size() >= filterRequest.getLimit();
        
        // 결과 Map 구성
        Map<String, Object> result = new HashMap<>();
        result.put("lectures", lectures);
        result.put("hasMore", hasMore);
        result.put("currentOffset", filterRequest.getOffset());
        result.put("nextOffset", filterRequest.getOffset() + lectures.size());
        result.put("loadedCount", lectures.size());
        result.put("filters", createFiltersMap(filterRequest));
        
        log.info("[getFilteredLectures] 조회된 강의 수: {}, 더보기 가능: {}", lectures.size(), hasMore);
        
        return result;
    }

    // 유저의 관심분야를 갖는 강의 조회
    public List<LectureSummary> selectMyInterestLecture(String userId){
        //데이터 수집/반환
        return lectureMapper.selectMyInterestLecture(userId);
    }

    // 선택한 관심분야를 갖는 강의 조회
    public List<LectureSummary> selectedInterestLecture(String categoryId){
        //데이터 수집/반환
        return lectureMapper.selectedInterestLecture(categoryId);
    }

    // 중간에 나오는 지식제공자 정보들
    public List<IpInfoSummary> ipInfoSummaries(){
        //데이터 수집&반환
        return informationProviderMapper.ipInfoSummaries();
    }


    // 강의 상세조회 페이지
    public Map<String, Object> lectureDetail(String lectureId){

        //강의 상세
        Lecture lectureDetail = lectureMapper.lectureDetail(lectureId);
        //강의의 수업 개수
        int contentNum = lectureMapper.contentNum(lectureId);
        //강의 목차
        List<Chapter> lectureChapter = lectureMapper.lectureChapter(lectureId);

        //해당 강의의 리뷰
        List<Review> lectureReviews = reviewMapper.lectureReviews(lectureId);

        Map<String, Object> result = new HashMap<>();
        result.put("lectureDetail",lectureDetail);
        result.put("contentNum",contentNum);
        result.put("lectureChapter",lectureChapter);
        result.put("lectureReviews",lectureReviews);

        return result;
    }

    // 목차의 추가자료
    public List<Addi> chapterAddi(String chapterId){
        return lectureMapper.chapterAddi(chapterId);
    }
    //목차의 영상자료
    public List<Video> chapterVideo(String chapterId){
        return lectureMapper.chapterVideo(chapterId);
    }
    //목차의 퀴즈
    public List<Question> chapterQuestion(String chapterId){
        return lectureMapper.chapterQuestion(chapterId);
    }

    // 강의 상세조회 - 리뷰쓰기
    public void regReview(RegReview review){
        lectureMapper.regReview(review);
    }

    // 강의 상세조회 - 평점순 리뷰
    public List<Review> lectureReviewsOrderByRating(String lectureId){
        return reviewMapper.lectureReviewsOrderByRating(lectureId);
    }

    // 강의 상세조회 - 평점순 리뷰 낮은거부터
    public List<Review> lectureReviewsOrderByRatingReverse(String lectureId){
        return reviewMapper.lectureReviewsOrderByRatingReverse(lectureId);
    }

    // 강의 상세조회 - 최신순 리뷰
    public List<Review> lectureReviewsRecent(String lectureId){
        return reviewMapper.lectureReviewsRecent(lectureId);
    }

    // 리뷰 수정하기
    @Transactional
    public boolean updateReview(UpdateReview updateReview) {
        String userId = getUserId();

        if (userId.equals(updateReview.getWriterId())) {
            // 이거로 인해서 아이디값은 따로 입력안받아도 됌
            updateReview.setWriterId(userId);

            int updatedRows = lectureMapper.updateReview(updateReview);

            if (updatedRows > 0) {
                log.info("[updateReview] 리뷰 수정 성공 - 리뷰ID: {}, 작성자: {}",
                        updateReview.getReviewId(), userId);
                return true;
            } else {
                log.error("[updateReview] 리뷰 수정 실패 - 리뷰ID: {}", updateReview.getReviewId());

                // 표준 예외: 데이터 수정 실패
                throw new IllegalStateException("리뷰 수정에 실패했습니다. 리뷰가 존재하지 않거나 이미 삭제되었을 수 있습니다.");
            }
        } else {
            log.error("수정하려는 계정 {}과 작성자의 계정이 일치하지 않습니다 {}",
                    updateReview.getWriterId(), userId);

            // 표준 예외: 접근 권한 없음
            throw new SecurityException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }
    }

    /**
     * 리뷰 삭제 (표준 예외 사용)
     */
    @Transactional
    public void delReview(String reviewId) {

        String userId = getUserId();
        String writerId = findWriterByReviewId(reviewId);

        // 권한 체크 예외 처리
        if (!userId.equals(writerId)) {
            log.warn("[delReview] 권한 없는 삭제 시도 - 요청자: {}, 작성자: {}, 리뷰: {}",
                    userId, writerId, reviewId);
            throw new SecurityException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // 삭제 실행 및 결과 확인
        Map<String, Object> params = Map.of("reviewId", reviewId, "writerId", writerId);

        lectureMapper.delReview(params);

        log.info("[delReview] 리뷰 삭제 성공 - 리뷰: {}, 사용자: {}", reviewId, userId);
    }

    // 강의 상세조회 - 찜 되어있는지 확인
    public boolean isBookmarked(String lectureId) {
        String userId = getCurrentUserId();

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("lectureId", lectureId);

        return lectureMapper.existsBookmark(params);
    }

    // 강의 상세조회 - 찜하기
    public void regBookmark(String lectureId){
        lectureMapper.regBookmark(lectureId, getCurrentUserId());
    }

    // 강의 상세조회 - 찜하기 삭제
    @Transactional
    public boolean removeBookmark(String lectureId) {
        String userId = getCurrentUserId();

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("lectureId", lectureId);

        int deletedRows = lectureMapper.deleteBookmark(params);
        return deletedRows > 0;
    }


    // 강사소개
    // 강사소개 강의에 필요한 강사 정보
    public IPInfo ipInfo(String userId){
        //데이터 수집&반환
        return informationProviderMapper.IPsInfo(userId);
    }
    // 지식제공자의 리뷰들
    public List<Review> ipReviews(String userId){
        return reviewMapper.ipReviews(userId);
    }
    // 지식제공자의 강의들
    public List<LectureSummary> ipLectures(String userId){
        return informationProviderMapper.ipLectures(userId);
    }



    // 유틸리티 메소드들
    public String findWriterByReviewId(String reviewId){
        return reviewMapper.findWriterByReviewId(reviewId);
    }

    /**
     * null 안전한 필터 맵 생성
     */
    private Map<String, Object> createFiltersMap(LectureFilterRequest filterRequest) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("categoryId", filterRequest.getCategoryId());
        filters.put("interestedId", filterRequest.getInterestedId());
        filters.put("priceType", filterRequest.getPriceType());
        filters.put("difficulty", filterRequest.getDifficulty());
        filters.put("orderBy", filterRequest.getOrderBy());
        filters.put("minRating", filterRequest.getMinRating());
        return filters;
    }
}
