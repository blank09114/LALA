package com.example.lala.API;

import com.example.lala.DTO.Request.MyComm;
import com.example.lala.DTO.Request.PageRequest;
import com.example.lala.DTO.Request.ProReqRequest;
import com.example.lala.DTO.Response.*;
import com.example.lala.Service.*;
import com.example.lala.exception.UserUpdateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 마이페이지 관련 기능을 제공하는 REST API 컨트롤러
 * 사용자 정보 수정, 프로필 이미지 업로드, 커뮤니티 관리 등의 기능을 포함합니다.
 */
@RestController
@RequestMapping("/users/mypage")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MypageAPI {

    private final MypageUserService mypageUserService;
    private final MypageService mypageService;
    private final CommunityService communityService;
    private final FileUploadService fileUploadService;
    private final LectureService lectureService;
    private final UserService userService;

    /**
     * 사용자 닉네임을 업데이트합니다.
     *
     * @param request 닉네임 정보가 담긴 요청 데이터
     * @return 업데이트 결과를 포함한 응답 Map
     * @throws UserUpdateException 닉네임 업데이트 실패 시
     */
    @PutMapping("/nickname")
    public ResponseEntity<Map<String, String>> updateNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        validateNickname(nickname);

        try {
            mypageUserService.updateNickname(nickname);
            log.info("닉네임 업데이트 성공: {}", nickname);
            return ResponseEntity.ok(createSuccessResponse("닉네임이 성공적으로 업데이트되었습니다."));
        } catch (Exception e) {
            log.error("닉네임 업데이트 실패", e);
            throw new UserUpdateException("닉네임 업데이트에 실패했습니다.");
        }
    }

    /**
     * 사용자 비밀번호를 업데이트합니다.
     *
     * @param request 현재 비밀번호와 새 비밀번호가 담긴 요청 데이터
     * @return 업데이트 결과를 포함한 응답 Map
     * @throws UserUpdateException 비밀번호 업데이트 실패 시
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("password");
        try {
            // 기존 비밀번호 확인
            boolean isCurrentPasswordValid = mypageUserService.verifyCurrentPassword(currentPassword);
            if (!isCurrentPasswordValid) {
                return ResponseEntity.badRequest().body(createErrorResponse("현재 비밀번호가 일치하지 않습니다."));
            }

            // 비밀번호 업데이트 수행
            mypageUserService.updatePassword(newPassword);
            log.info("비밀번호 업데이트 성공");
            return ResponseEntity.ok(createSuccessResponse("비밀번호가 성공적으로 업데이트되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 업데이트 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 업데이트 실패", e);
            throw new UserUpdateException("비밀번호 업데이트에 실패했습니다.");
        }
    }

    /**
     * 사용자 프로필 이미지를 업데이트합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 URL을 포함한 응답 Map
     * @throws FileUploadException 파일 업로드 실패 시
     */
    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, Object>> updateProfileImage(
            @RequestParam("file") MultipartFile file) throws FileUploadException {
        validateProfileImageFile(file);

        try {
            String fileNameURL = fileUploadService.saveProfileImage(file);
            log.info("프로필 이미지 업데이트 성공: {}", fileNameURL);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "프로필 사진이 성공적으로 업데이트되었습니다.",
                    "imageUrl", fileNameURL
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("프로필 이미지 업로드 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패", e);
            throw new FileUploadException("프로필 이미지 업데이트에 실패했습니다.");
        }
    }

    /**
     * 리뷰를 업데이트합니다.
     *
     * @return 업데이트 결과를 포함한 응답 Map
     */
    @PutMapping("/review")
    public ResponseEntity<Map<String, String>> updateReview(@RequestBody Map<String, Object> request) {
        String reviewId = (String) request.get("reviewId");
        String content = (String) request.get("content");
        Integer rate = (Integer) request.get("rate");

        log.info("리뷰 수정 요청 - 전체 데이터: {}", request);
        log.info("수정 요청 데이터: reviewId={}, content={}, rate={}", reviewId, content, rate);

        Map<String, Object> params = new HashMap<>();
        params.put("reviewId", reviewId);
        params.put("content", content);
        params.put("rate", rate);

        try {
            int result = mypageService.updateReview(params);
            if (result > 0) {
                return ResponseEntity.ok(createSuccessResponse("리뷰가 성공적으로 수정되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("리뷰를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("리뷰 수정 실패", e);
            return ResponseEntity.badRequest().body(createErrorResponse("리뷰 수정에 실패했습니다."));
        }
    }

    /**
     * 수강평 확인 API
     *
     * @param request 강의 ID가 담긴 요청 데이터
     * @return 기존 수강평 정보
     */
    @PostMapping("/check-review")
    public ResponseEntity<MyReview> checkReview(@RequestBody Map<String, String> request) {
        String lectureId = request.get("lectureId");

        try {
            MyReview review = mypageService.checkReview(lectureId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("수강평 확인 실패", e);
            return ResponseEntity.ok(new MyReview()); // 빈 객체 반환
        }
    }

    /**
     * 리뷰를 작성.
     *
     * @return 업데이트 결과를 포함한 응답 Map
     */
    @PostMapping("/review")
    public ResponseEntity<Map<String, String>> insertReview(@RequestBody Map<String, Object> request) {
        log.debug("insertReview: {}", request);
        String lectureId = (String) request.get("lectureId");
        String content = (String) request.get("content");
        Integer rate = (Integer) request.get("rating");
        System.out.println("request: " + request);
        Map<String, Object> params = new HashMap<>();
        params.put("lectureId", lectureId);
        params.put("content", content);
        params.put("rate", rate);
        try {
            int result = mypageService.insertReview(params);
            return ResponseEntity.ok(createSuccessResponse("리뷰가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("리뷰 수정에 실패했습니다."));
        }
    }

    /**
     * 사용자가 작성한 커뮤니티 글을 조회합니다.
     *
     * @param myComm 커뮤니티 조회 조건
     * @return 사용자의 커뮤니티 글 목록
     * @throws RuntimeException 커뮤니티 데이터 조회 실패 시
     */
    @PostMapping("/community")
    public ResponseEntity<PageResponse<MyCommunity>> getMyComm(@RequestBody MyComm myComm) {
        try {
            validateMyComm(myComm);
            PageResponse<MyCommunity> result = communityService.selectMyComm(myComm);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("커뮤니티 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reported")
    public ResponseEntity<PageResponse<MyCommunity>> getReportedComm(@RequestBody Map<String, Object> request) {
        try {
            Integer[] postType = validateAndExtractPostType(request);

            // 페이지 요청 정보 추출
            PageRequest pageRequest = new PageRequest(1, 10); // 기본값
            if (request.containsKey("page")) {
                int page = (Integer) request.getOrDefault("page", 1);
                int size = (Integer) request.getOrDefault("size", 10);
                pageRequest = new PageRequest(page, size);
            }

            PageResponse<MyCommunity> result = communityService.selectReportedComm(postType, pageRequest);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("신고한글 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자가 관심표시한 강의 목록을 조회합니다.
     *
     * @param request 강의 조회 조건 (무료/유료 구분)
     * @return 관심표시한 강의 목록
     */
    @PostMapping("/like-lecture")
    public ResponseEntity<Map<String, Object>> getLikeLecture(@RequestBody Map<String, Object> request) {
        Boolean free = (Boolean) request.getOrDefault("free", false);
        int page = request.get("page") != null ? (Integer) request.get("page") : 1;
        int size = request.get("size") != null ? (Integer) request.get("size") : 10;

        try {
            // 전체 데이터 조회
            List<LikeLecture> allLectures = lectureService.selectLikeLecture(free);
            int totalElements = allLectures.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // 페이지네이션 적용
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            List<LikeLecture> pageLectures = totalElements > 0 ? allLectures.subList(startIndex, endIndex) : new ArrayList<>();

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageLectures);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("관심강의 조회 실패", e);
            throw new RuntimeException("관심강의 데이터를 조회할 수 없습니다.");
        }
    }

    /**
     * 사용자가 관심표시한 강의 제공자 목록을 조회합니다.
     *
     * @param request 페이지 정보가 담긴 요청 데이터
     * @return 관심표시한 강의 제공자 목록과 페이지네이션 정보
     */
    @PostMapping("/like-provider")
    public ResponseEntity<Map<String, Object>> getLikeProvider(@RequestBody Map<String, Integer> request) {
        int page = request.getOrDefault("page", 1);
        int size = request.getOrDefault("size", 10);

        try {
            List<LikeProvider> providers = mypageService.getLikeProvider(page, size);

            // 전체 개수 조회 (페이지네이션을 위한)
            List<LikeProvider> allProviders = mypageService.getLikeProvider();
            int totalElements = allProviders.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            Map<String, Object> response = new HashMap<>();
            response.put("content", providers);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);
            response.put("hasNext", page < totalPages);
            response.put("hasPrevious", page > 1);

            log.info("관심 제공자 조회 성공: {} 건 (page: {}, size: {}, total: {})",
                    providers.size(), page, size, totalElements);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("관심 제공자 조회 실패", e);
            throw new RuntimeException("관심 제공자 데이터를 조회할 수 없습니다.");
        }
    }

    /**
     * 사용자의 수강 강의 목록을 조회합니다.
     *
     * @param request 강의 조회 조건 (필터 타입)
     * @return 사용자의 수강 강의 목록
     */
    @PostMapping("/user-lecture")
    public ResponseEntity<List<MyLearning>> getUserLecture(@RequestBody Map<String, Integer> request) {
        int filterType = request.getOrDefault("filterType", 0);

        try {
            List<MyLearning> result = mypageService.getUserLecture(filterType);
            log.info("사용자 강의 조회 성공: {} 건 (filterType: {})", result.size(), filterType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("사용자 강의 조회 실패", e);
            throw new RuntimeException("사용자 강의 데이터를 조회할 수 없습니다.");
        }
    }

    /**
     * 사용자가 취소한 강의 목록을 페이지네이션으로 조회합니다.
     *
     * @param request 페이지 정보가 담긴 요청 데이터
     * @return 취소한 강의 목록과 페이지네이션 정보
     */
    @PostMapping("/CancelLecture")
    public ResponseEntity<Map<String, Object>> getUserCancelLecture(@RequestBody Map<String, Integer> request) {
        try {
            // 입력값 검증
            if (request == null) {
                log.warn("요청 데이터가 null입니다.");
                throw new IllegalArgumentException("요청 데이터가 필요합니다.");
            }

            int page = request.getOrDefault("page", 1);
            int size = request.getOrDefault("size", 8); // 한 페이지에 8개씩 표시
            System.out.println("page" + page);
            System.out.println("size" + size);

            // 페이지 값 검증
            if (page < 1) {
                log.warn("잘못된 페이지 번호: {}", page);
                page = 1;
            }
            if (size < 1 || size > 100) {
                log.warn("잘못된 페이지 크기: {}", size);
                size = 8;
            }

            log.info("취소 강의 조회 요청 - 페이지: {}, 크기: {}", page, size);

            // 총 개수 조회
            int totalCount;
            try {
                totalCount = mypageService.getCancelLectureCount();
                log.debug("총 취소 강의 개수: {}", totalCount);
            } catch (Exception e) {
                log.error("취소 강의 개수 조회 실패", e);
                throw new RuntimeException("취소 강의 개수를 조회할 수 없습니다.", e);
            }

            // 페이지네이션 적용하여 데이터 조회
            List<CancelLecture> result;
            try {
                result = mypageService.getCancelLecture(page, size);
                log.debug("취소 강의 데이터 조회 완료: {} 건", result != null ? result.size() : 0);
            } catch (Exception e) {
                log.error("취소 강의 데이터 조회 실패", e);
                throw new RuntimeException("취소 강의 데이터를 조회할 수 없습니다.", e);
            }

            // 결과 데이터 검증
            if (result == null) {
                log.warn("취소 강의 데이터가 null입니다.");
                result = new ArrayList<>();
            }

            // 페이지네이션 정보 계산
            int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / size) : 0;

            Map<String, Object> response = new HashMap<>();
            response.put("content", result);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalElements", totalCount);
            response.put("pageSize", size);
            response.put("hasNext", page < totalPages);
            response.put("hasPrevious", page > 1);

            log.info("취소 강의 조회 성공: {} 건 (페이지: {}/{})", result.size(), page, totalPages);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 파라미터", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "잘못된 요청 파라미터");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (RuntimeException e) {
            log.error("취소 강의 조회 중 런타임 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류");
            errorResponse.put("message", "취소 강의 데이터를 조회할 수 없습니다.");
            return ResponseEntity.status(500).body(errorResponse);

        } catch (Exception e) {
            log.error("취소 강의 조회 중 예상치 못한 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "예상치 못한 오류");
            errorResponse.put("message", "시스템 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ==================== Validation Methods ====================

    /**
     * 닉네임의 유효성을 검증합니다.
     *
     * @param nickname 검증할 닉네임
     * @throws IllegalArgumentException 닉네임이 null이거나 공백인 경우
     */
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 입력 항목입니다.");
        }
        if (nickname.trim().length() > 20) {
            throw new IllegalArgumentException("닉네임은 20자를 초과할 수 없습니다.");
        }
    }

    /**
     * 비밀번호의 유효성을 검증합니다.
     *
     * @param password 검증할 비밀번호
     * @throws IllegalArgumentException 비밀번호가 null이거나 공백인 경우
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 항목입니다.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }

    /**
     * 프로필 이미지 파일의 유효성을 검증합니다.
     *
     * @param file 검증할 파일
     * @throws IllegalArgumentException 파일이 null이거나 비어있는 경우
     */
    private void validateProfileImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 제한 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 이미지 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 리뷰 데이터의 유효성을 검증합니다.
     *
     * @param review 검증할 리뷰
     * @throws IllegalArgumentException 리뷰가 null이거나 리뷰 ID가 null인 경우
     */
    private void validateReview(Review review) {
        if (review == null || review.getReviewId() == null) {
            throw new IllegalArgumentException("유효하지 않은 리뷰 정보입니다.");
        }
    }

    /**
     * 커뮤니티 조회 조건의 유효성을 검증합니다.
     *
     * @param myComm 검증할 커뮤니티 조회 조건
     * @throws IllegalArgumentException 커뮤니티 조회 조건이 null인 경우
     */
    private void validateMyComm(MyComm myComm) {
        if (myComm == null) {
            throw new IllegalArgumentException("커뮤니티 조회 조건이 필요합니다.");
        }
    }

    private Integer[] validateAndExtractPostType(Map<String, Object> request) {
        Object postTypeObj = request.get("postType");
        if (postTypeObj == null) {
            throw new IllegalArgumentException("postType은 필수입니다.");
        }

        if (postTypeObj instanceof List<?> list) {
            return list.stream()
                    .map(obj -> (Integer) obj)
                    .toArray(Integer[]::new);
        } else if (postTypeObj instanceof Integer[]) {
            return (Integer[]) postTypeObj;
        } else {
            throw new IllegalArgumentException("postType 형식이 올바르지 않습니다.");
        }
    }

    // ==================== Response Helper Methods ====================

    /**
     * 성공 응답 Map을 생성합니다.
     *
     * @param message 성공 메시지
     * @return 성공 응답 Map
     */
    private Map<String, String> createSuccessResponse(String message) {
        return Map.of(
                "status", "success",
                "message", message
        );
    }

    /**
     * 에러 응답 Map을 생성합니다.
     *
     * @param message 에러 메시지
     * @return 에러 응답 Map
     */
    private Map<String, String> createErrorResponse(String message) {
        return Map.of(
                "status", "error",
                "message", message
        );
    }

    /**
     * 비밀번호 변경 요청의 유효성을 검증합니다.
     *
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     * @throws IllegalArgumentException 검증 실패 시
     */
    private void validatePasswordChange(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("현재 비밀번호는 필수 입력 항목입니다.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호는 필수 입력 항목입니다.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
    }

    @GetMapping("/information-provider")
    public ResponseEntity<InformationProvider> getInformationProvider() {
        try {
            InformationProvider provider = mypageService.selectInformationProvider();
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            log.error("지식 제공자 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/external-links")
    public ResponseEntity<Map<String, String>> updateExternalLinks(@RequestBody Map<String, String> request) {
        String externalLink = request.get("externalLink");
        System.out.println("externalLink" + externalLink);
        try {
            mypageService.updateExternalLink(externalLink);
            return ResponseEntity.ok(createSuccessResponse("외부 링크가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            log.error("외부 링크 수정 실패", e);
            return ResponseEntity.badRequest().body(createErrorResponse("외부 링크 수정에 실패했습니다."));
        }
    }

    @PutMapping("/introduction")
    public ResponseEntity<Map<String, String>> updateIntroduction(@RequestBody Map<String, String> request) {
        String introduction = request.get("introduction");
        try {
            mypageService.updateIntroduction(introduction);
            return ResponseEntity.ok(createSuccessResponse("외부 링크가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            log.error("외부 링크 수정 실패", e);
            return ResponseEntity.badRequest().body(createErrorResponse("외부 링크 수정에 실패했습니다."));
        }
    }

    @PostMapping("/pro-req")
    public ResponseEntity<Map<String, String>> insertProReq(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String nickname = request.get("nickname");
        String intro = request.get("intro");
        String externallink = request.get("externallink");
        try {
            mypageService.insertProReq(name, nickname, intro, externallink);
            return ResponseEntity.ok(createSuccessResponse("지식 제공자 신청이 성공적으로 제출되었습니다."));
        } catch (Exception e) {
            log.error("지식 제공자 신청 실패", e);
            return ResponseEntity.badRequest().body(createErrorResponse("지식 제공자 신청에 실패했습니다."));
        }
    }

    /**
     * 학력/약력과 첨부파일을 포함한 지식 제공자 신청을 처리합니다.
     *
     * @param name         실명
     * @param nickname     활동명
     * @param intro        소개
     * @param externallink 외부 링크
     * @param historyTexts 학력/약력 텍스트 배열
     * @param historyFiles 학력/약력 파일 배열
     * @return ResponseEntity 처리 결과와 학력/약력 데이터
     */
    @PostMapping("/pro-req-with-history")
    public ResponseEntity<Map<String, Object>> insertProReqWithHistory(
            @RequestParam("name") String name,
            @RequestParam("nickname") String nickname,
            @RequestParam("intro") String intro,
            @RequestParam("externallink") String externallink,
            @RequestParam(value = "historyTexts", required = false) List<String> historyTexts,
            @RequestParam(value = "historyFiles", required = false) List<MultipartFile> historyFiles) {

        try {
            // 디버깅: 받은 파라미터 로그 출력
            log.info("받은 파라미터 - name: {}, nickname: {}, intro: {}", name, nickname, intro);
            log.info("받은 historyTexts: {}", historyTexts);
            log.info("받은 historyFiles 개수: {}", historyFiles != null ? historyFiles.size() : 0);

            // 필수 입력값 검증
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "이름은 필수 입력 항목입니다."
                ));
            }
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "닉네임은 필수 입력 항목입니다."
                ));
            }
            if (intro == null || intro.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "소개는 필수 입력 항목입니다."
                ));
            }

            // 학력/약력 데이터 처리
            List<ProReqRequest.HistoryItem> historyItems = null;
            if (historyTexts != null && !historyTexts.isEmpty()) {
                historyItems = new ArrayList<>();

                for (int i = 0; i < historyTexts.size(); i++) {
                    String text = historyTexts.get(i);
                    MultipartFile file = (historyFiles != null && i < historyFiles.size()) ? historyFiles.get(i) : null;

                    log.info("처리 중인 항목 {} - text: {}, file: {}", i, text, file != null ? file.getOriginalFilename() : "null");

                    ProReqRequest.HistoryItem item = ProReqRequest.HistoryItem.builder()
                            .text(text)
                            .file(file)
                            .build();
                    historyItems.add(item);
                }
            }

            Map<String, Object> result = mypageService.insertProReqWithHistory(name, nickname, intro, externallink, historyItems);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("지식 제공자 신청 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "지식 제공자 신청에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자의 학력/약력 정보를 조회합니다.
     *
     * @return ResponseEntity 학력/약력 정보 리스트
     */
    @GetMapping("/pro-req-history")
    public ResponseEntity<Map<String, Object>> getProReqHistory() {
        try {
            List<Map<String, Object>> historyList = mypageService.getProReqHistory();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "학력/약력 정보 조회가 완료되었습니다.");
            response.put("historyData", historyList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("학력/약력 정보 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "학력/약력 정보 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자가 작성한 리뷰 목록을 페이지네이션으로 조회합니다.
     *
     * @param request 페이지 정보가 담긴 요청 데이터
     * @return 사용자의 리뷰 목록과 페이지네이션 정보
     */
    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getMyReviews(@RequestBody Map<String, Integer> request) {
        int page = request.getOrDefault("page", 1);
        int size = request.getOrDefault("size", 3);

        try {
            // 페이지 번호 검증 (1부터 시작)
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 3;
            }

            // 전체 리뷰 목록 조회
            List<Review> allReviews = mypageService.getMyReviews();
            int totalElements = allReviews.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // 현재 페이지가 전체 페이지를 초과하는 경우 마지막 페이지로 설정
            if (page > totalPages && totalPages > 0) {
                page = totalPages;
            }

            // 페이지네이션 적용 (0-based 인덱스로 변환)
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<Review> pageReviews = totalElements > 0 ?
                    allReviews.subList(startIndex, endIndex) :
                    new ArrayList<>();

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("content", pageReviews);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);
            response.put("hasNext", page < totalPages);
            response.put("hasPrevious", page > 1);
            response.put("first", page == 1);
            response.put("last", page == totalPages || totalPages == 0);

            log.info("리뷰 목록 조회 성공: {} 건 (page: {}, size: {}, total: {})",
                    pageReviews.size(), page, size, totalElements);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리뷰 목록 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "리뷰 목록을 조회할 수 없습니다.");
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", 1);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 강의 제공자의 강의 목록을 페이지네이션과 함께 조회합니다.
     *
     * @param free 무료 강의 여부
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 강의 목록과 페이지네이션 정보
     */
    @GetMapping("/pro_lecture")
    public ResponseEntity<Map<String, Object>> getProLectures(
            @RequestParam("free") boolean free,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {

        try {
            // 페이지는 1부터 시작하므로 0부터 시작하는 인덱스로 변환
            int offset = (page - 1) * size;

            // 강의 목록 조회
            List<IPLecture> lectures = mypageService.getIPLectureWithPagination(free, offset, size);

            // 전체 강의 수 조회
            int totalElements = mypageService.getIPLectureCount(free);
            int totalPages = (int) Math.ceil((double) totalElements / size);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", lectures);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("강의 목록 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "강의 목록을 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<Map<String, Object>> purchaseLecture(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> lectureIds = (List<String>) request.get("lectureIds");

            if (lectureIds == null || lectureIds.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "구매할 강의를 선택해주세요.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = mypageService.purchaseLecture(lectureIds);

            if ((Boolean) result.get("success")) {
                log.info("강의 구매 성공 - 성공: {}, 실패: {}", result.get("successCount"), result.get("failCount"));
                return ResponseEntity.ok(result);
            } else {
                log.warn("강의 구매 실패 - 실패: {}", result.get("failCount"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("강의 구매 처리 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "구매 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getPrifileImage() {
        try {
            // 현재 로그인한 사용자 정보 조회
            UserProfile userProfile = mypageService.getCurrentUserProfile();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profilename", userProfile.getProfileName());
            response.put("nickname", userProfile.getUserNickname());
            
            log.info("프로필 이미지 조회 성공 - userId: {}, profileName: {}", userProfile.getUserNickname(), userProfile.getProfileName());
            
            // 캐시 방지 헤더 추가
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("사용자를 찾을 수 없음");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자를 찾을 수 없습니다.");
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("프로필 이미지 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "프로필 이미지 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}