package com.example.lala.API;

import com.example.lala.DTO.Course.RegReview;
import com.example.lala.DTO.Course.UpdateReview;
import com.example.lala.DTO.Request.LectureFilterRequest;
import com.example.lala.Service.CourseService;
import com.example.lala.Service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseAPI {

    private final CourseService courseService;

    // 메인 페이지 데이터
    @GetMapping("/main/data")
    public ResponseEntity<?> getMainLectureData() {
        String userId = courseService.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("studyPlanner", courseService.getStudyPlanner(userId));
        result.put("recentLecture", courseService.recentLecture(userId));
        result.put("interestLectures", courseService.selectMyInterestLecture(userId));
//        result.put("selectLastUpdate", courseService.selectLastUpdate(userId));
        result.put("informationProviders", courseService.ipInfoSummaries());
        return ResponseEntity.ok(result);
    }

    // ✨ 새로 추가: 필터링된 강의 조회 (더보기 방식)
    @GetMapping("/lectures/filtered")
    public ResponseEntity<?> getFilteredLectures(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String interestedId,
            @RequestParam(required = false, defaultValue = "all") String priceType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "40") Integer limit) {

        try {
            // 필터링 요청 DTO 생성
            LectureFilterRequest filterRequest = LectureFilterRequest.builder()
                    .categoryId(categoryId)
                    .interestedId(interestedId)
                    .priceType(priceType)
                    .difficulty(difficulty)
                    .orderBy(orderBy)
                    .minRating(minRating)
                    .offset(offset)
                    .limit(limit)
                    .build();

            // 서비스 호출
            Map<String, Object> result = courseService.getFilteredLectures(filterRequest);

            log.info("[getFilteredLectures] 필터링 조회 성공 - 조건: {}", filterRequest);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[getFilteredLectures] 필터링 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorMap("강의 조회 중 오류가 발생했습니다.", e.getMessage()));
        }
    }

    // ✨ 새로 추가: 빠른 필터 조회 (무료/유료/할인 강의만)
    @GetMapping("/lectures/quick-filter")
    public ResponseEntity<?> getQuickFilterLectures(
            @RequestParam String type, // "free", "paid", "discount"
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "12") Integer limit) {

        try {
            LectureFilterRequest filterRequest = LectureFilterRequest.builder()
                    .priceType(type)
                    .orderBy(orderBy)
                    .offset(offset)
                    .limit(limit)
                    .build();

            Map<String, Object> result = courseService.getFilteredLectures(filterRequest);

            log.info("[getQuickFilterLectures] 빠른 필터 조회 성공 - 타입: {}", type);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[getQuickFilterLectures] 빠른 필터 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorMap("강의 조회 중 오류가 발생했습니다.", e.getMessage()));
        }
    }

    // ✨ 새로 추가: 난이도별 강의 조회
    @GetMapping("/lectures/by-difficulty/{difficulty}")
    public ResponseEntity<?> getLecturesByDifficulty(
            @PathVariable String difficulty,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "12") Integer limit) {

        try {
            LectureFilterRequest filterRequest = LectureFilterRequest.builder()
                    .difficulty(difficulty)
                    .orderBy(orderBy)
                    .offset(offset)
                    .limit(limit)
                    .build();

            Map<String, Object> result = courseService.getFilteredLectures(filterRequest);

            log.info("[getLecturesByDifficulty] 난이도별 조회 성공 - 난이도: {}", difficulty);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[getLecturesByDifficulty] 난이도별 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorMap("강의 조회 중 오류가 발생했습니다.", e.getMessage()));
        }
    }

    // ✨ 새로 추가: 키워드 검색 강의 조회
    @GetMapping("/lectures/search")
    public ResponseEntity<?> searchLectures(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "40") Integer limit) {

        try {
            LectureFilterRequest filterRequest = LectureFilterRequest.builder()
                    .keyword(keyword.trim())
                    .orderBy(orderBy)
                    .offset(offset)
                    .limit(limit)
                    .build();

            Map<String, Object> result = courseService.getFilteredLectures(filterRequest);

            log.info("[searchLectures] 키워드 검색 성공 - 키워드: {}", keyword);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[searchLectures] 키워드 검색 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorMap("강의 검색 중 오류가 발생했습니다.", e.getMessage()));
        }
    }

    // 전체 강의 조회 (기존 방식 - 호환성 유지)
    @GetMapping("/main/allLectures")
    public ResponseEntity<?> allLectures(){
        Map<String, Object> result = new HashMap<>();
        result.put("allLectures",courseService.allLectures());
        return ResponseEntity.ok(result);
    }


    //카테고리 선택하면 선택한 카테고리 갖는 강의들
    @GetMapping("/main/data/{categoryId}")
    public ResponseEntity<?> selectedCategory(@PathVariable String categoryId) {
        Map<String, Object> result = new HashMap<>();
        result.put("selectedInterestLecture", courseService.selectedInterestLecture(categoryId));
        return ResponseEntity.ok(result);
    }

    // 강의 상세 페이지 (강의 상세, 강의의 수업 갯수, 목차, 리뷰)
    @GetMapping("/detail/data/{lectureId}")
    public ResponseEntity<?> getLectureDetail(@PathVariable String lectureId) {
        return ResponseEntity.ok(courseService.lectureDetail(lectureId));
    }

    // 강의 상세에 목차 누르면 나오는 데이터
    @GetMapping("/chapter-contents/{chapterId}")
    public ResponseEntity<?> getChapterContents(@PathVariable String chapterId) {
        Map<String, Object> result = new HashMap<>();
        result.put("addiFiles", courseService.chapterAddi(chapterId));
        result.put("videos", courseService.chapterVideo(chapterId));
        result.put("questions", courseService.chapterQuestion(chapterId));
        return ResponseEntity.ok(result);
    }

    // 리뷰 작성하기     ✅
    @PostMapping("/regReview")
    public ResponseEntity<?> registerReview(@RequestBody RegReview review) {
        courseService.regReview(review);
        return ResponseEntity.ok(Map.of("message", "리뷰 등록 완료"));
    }

    // 리뷰 수정하기
    @PutMapping("/regUpdate")
    public ResponseEntity<Map<String, Object>> updateReview(@RequestBody UpdateReview updateReview) {
        try {
            boolean success = courseService.updateReview(updateReview);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "리뷰가 성공적으로 수정되었습니다."
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "리뷰 수정에 실패했습니다."
                        ));
            }

        } catch (RuntimeException e) {
            log.warn("[updateReview] 권한 오류: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // 리뷰 조회하기 (최신순)
    @GetMapping("/review/recent/{lectureId}")
    public ResponseEntity<?> showReviewsRecent(@PathVariable String lectureId) {
        return ResponseEntity.ok(courseService.lectureReviewsRecent(lectureId));
    }

    // 리뷰 조회하기 (높은 평점순)
    @GetMapping("/review/row-rate/{lectureId}")
    public ResponseEntity<?> showReviewsOrderbyRating(@PathVariable String lectureId) {
        return ResponseEntity.ok(courseService.lectureReviewsOrderByRating(lectureId));
    }

    // 리뷰 조회하기 (낮은 평점순)
    @GetMapping("/review/high-rate/{lectureId}")
    public ResponseEntity<?> lectureReviewsOrderByRatingReverse(@PathVariable String lectureId) {
        return ResponseEntity.ok(courseService.lectureReviewsOrderByRatingReverse(lectureId));
    }

    // 리뷰 삭제하기      ✅
    @DeleteMapping("/delReview/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId) {
        courseService.delReview(reviewId);
        return ResponseEntity.ok(Map.of("message", "리뷰 삭제 완료"));
    }

    // 찜하기    ✅
    @PostMapping("/bookmarking/{lectureId}")
    public ResponseEntity<?> regBookmark(@PathVariable String lectureId) {
        courseService.regBookmark(lectureId);
        return ResponseEntity.ok(Map.of(
                "message", "북마크 등록 완료",
                "lectureId", lectureId
        ));
    }

    // 찜 되어있는지 확인하기
    @GetMapping("/isBookmarked/{lectureId}")
    public ResponseEntity<?> isBookmarked(@PathVariable String lectureId) {
        boolean result = courseService.isBookmarked(lectureId);

        if (result) {
            return ResponseEntity.ok(Map.of(
                    "message", "북마크를 해놓으셨습니다잇"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "isBookmarked", false,
                    "message", "북마크하지 않으셨습니다"
            ));
        }
    }

    // 찜 삭제하기
    @DeleteMapping("/delBookmark/{lectureId}")
    public ResponseEntity<?> delBookmark(@PathVariable String lectureId) {
        boolean resutl = courseService.removeBookmark(lectureId);

        if (resutl) {
            return ResponseEntity.ok(Map.of(
                    "message", "찜 삭제 완료"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "isBookmarked", false,
                    "message", "찜 삭제가 성공적으로 이뤄지지 않았습니다"
            ));
        }
    }

    // 지식 제공자 페이지 데이터
    @GetMapping("/providerDetail/{providerId}")
    public ResponseEntity<?> providerDetail (@PathVariable String providerId){
        //지식제공자의 정보, 리뷰들, 강의들 조회하기
        return ResponseEntity.ok(Map.of(
                "infos", courseService.ipInfo(providerId),
                "reviews", courseService.ipReviews(providerId),
                "lectures", courseService.ipLectures(providerId)
        ));
    }

    /**
     * null 안전한 에러 맵 생성
     */
    private Map<String, Object> createErrorMap(String error, String message) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", error);
        errorMap.put("message", message);
        return errorMap;
    }

}