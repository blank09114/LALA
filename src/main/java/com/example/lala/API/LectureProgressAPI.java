package com.example.lala.API;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Lms.LectureProgressInfo;
import com.example.lala.Service.LectureProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 📊 LMS 수강률 전용 REST API Controller
 *
 * 📌 주요 기능:
 * - LMS 페이지 진입 시 사용자 타입별 UI 정보 제공
 * - 자료 클릭 시 진도율 업데이트 
 * - RNB(네비게이션바) 진도율 표시
 * - 강의 취소 기능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class LectureProgressAPI extends BaseService {

    private final LectureProgressService progressService;

    /**
     * 🎯 LMS 페이지 진입 시 사용자 타입별 정보 조회
     *
     * ✨ 로직:
     * - 지식제공자: 수정 버튼 표시
     * - 일반 사용자 (진도율 0%): 취소 버튼 표시
     * - 일반 사용자 (진도율 1% 이상): 진도율 바 표시
     *
     * @param lectureId 강의 ID
     * @return UI 표시 정보
     */
    @GetMapping("/lms-info/{lectureId}")
    public ResponseEntity<Map<String, Object>> getLMSPageInfo(@PathVariable String lectureId) {
        try {
            // 현재 로그인한 사용자 ID 가져오기
            String userId = getCurrentUserId();

            log.info("[getLMSPageInfo] 호출 - 사용자: {}, 강의: {}", userId, lectureId);

            // 서비스에서 사용자 타입별 정보 조회
            Map<String, Object> result = progressService.getLMSPageInfo(userId, lectureId);

            if ((Boolean) result.get("success")) {
                log.info("[getLMSPageInfo] 성공 - 사용자 타입: {}", result.get("userType"));
                return ResponseEntity.ok(result);
            } else {
                log.error("[getLMSPageInfo] 실패 - {}", result.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch (Exception e) {
            log.error("[getLMSPageInfo] 예외 발생: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "LMS 페이지 정보 조회에 실패했습니다: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 📚 자료 클릭 시 진도율 업데이트 (일반 사용자만)
     *
     * 로직:
     * - 지식제공자가 자료 클릭: 진도율 업데이트 안함
     * - 일반 사용자가 자료 클릭: completed_materials 기록 + 진도율 계산 및 업데이트
     *
     * @param lectureId 강의 ID
     * @param materialId 자료 ID
     * @return 업데이트된 진도율 정보
     */
    @PostMapping("/update/{lectureId}/{materialId}")
    public ResponseEntity<Map<String, Object>> updateMaterialProgress(
            @PathVariable String lectureId,
            @PathVariable String materialId) {

        try {
            String userId = getCurrentUserId();

            log.info("[updateMaterialProgress] 호출 - 사용자: {}, 강의: {}, 자료: {}",
                    userId, lectureId, materialId);

            // 진도율 업데이트
            LectureProgressInfo progressInfo = progressService.updateMaterialProgress(userId, lectureId, materialId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "진도율이 업데이트되었습니다");
            response.put("progressRate", progressInfo.getProgressRate());
            response.put("lectureName", progressInfo.getLectureName());
            response.put("lastUpdateDate", progressInfo.getLastUpdateDate());

            log.info("[updateMaterialProgress] 성공 - 진도율: {}%", progressInfo.getProgressRate());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("[updateMaterialProgress] 업무 로직 오류: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("[updateMaterialProgress] 시스템 오류: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "진도율 업데이트에 실패했습니다");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * RNB(우상단 네비게이션바) 진도율 조회
     *
     * 로직:
     * - 지식제공자: null 반환 (진도율 표시 안함)
     * - 일반 사용자: 현재 진도율 반환
     *
     * @param lectureId 강의 ID
     * @return 진도율 정보 (지식제공자는 null)
     */
    @GetMapping("/rnb/{lectureId}")
    public ResponseEntity<Map<String, Object>> getRNBProgress(@PathVariable String lectureId) {
        try {
            String userId = getCurrentUserId();

            log.info("[getRNBProgress] 호출 - 사용자: {}, 강의: {}", userId, lectureId);

            LectureProgressInfo progressInfo = progressService.getRNBProgress(userId, lectureId);

            Map<String, Object> response = new HashMap<>();

            if (progressInfo != null) {
                // 일반 사용자 - 진도율 표시
                response.put("success", true);
                response.put("showProgress", true);
                response.put("progressRate", progressInfo.getProgressRate());
                response.put("lectureName", progressInfo.getLectureName());

                log.info("[getRNBProgress] 일반 사용자 - 진도율: {}%", progressInfo.getProgressRate());

            } else {
                // 지식제공자 - 진도율 표시 안함
                response.put("success", true);
                response.put("showProgress", false);
                response.put("message", "지식제공자는 진도율이 표시되지 않습니다");

                log.info("[getRNBProgress] 지식제공자 - 진도율 표시 안함");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[getRNBProgress] 오류: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("showProgress", false);
            response.put("message", "진도율 조회에 실패했습니다");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 강의 취소 (진도율 0%인 일반 사용자만)
     *
     * 로직:
     * - 진도율이 0%인 경우에만 취소 가능
     * - user_lecture 테이블에서 해당 강의 삭제
     *
     * @param lectureId 강의 ID
     * @return 취소 성공 여부
     */
    @DeleteMapping("/cancel/{lectureId}")
    public ResponseEntity<Map<String, Object>> cancelLecture(@PathVariable String lectureId) {
        try {
            String userId = getCurrentUserId();

            log.info("[cancelLecture] 호출 - 사용자: {}, 강의: {}", userId, lectureId);

            boolean success = progressService.cancelUserLecture(userId, lectureId);

            Map<String, Object> response = new HashMap<>();

            if (success) {
                response.put("success", true);
                response.put("message", "강의가 성공적으로 취소되었습니다");

                log.info("[cancelLecture] 성공 - 강의 취소 완료");

                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "강의 취소에 실패했습니다");

                log.error("[cancelLecture] 실패");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("[cancelLecture] 오류: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "강의 취소 중 오류가 발생했습니다");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 강의 생성 완료 처리
     *
     * 로직:
     * - 강의 상태를 2(승인 요청 중)로 변경
     *
     * @param lectureId 강의 ID
     * @return 처리 결과
     */
    @PostMapping("/complete-creation/{lectureId}")
    public ResponseEntity<Map<String, Object>> completeLectureCreation(@PathVariable String lectureId) {
        try {
            log.info("[completeLectureCreation] 호출 - 강의: {}", lectureId);

            progressService.completeLectureCreation(lectureId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "강의 생성이 완료되었습니다");

            log.info("[completeLectureCreation] 성공 - 강의 상태 변경 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[completeLectureCreation] 오류: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "강의 생성 완료 처리에 실패했습니다");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}