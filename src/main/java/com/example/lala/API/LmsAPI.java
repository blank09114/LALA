package com.example.lala.API;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Lms.*;
import com.example.lala.DTO.Response.UserProfile;
import com.example.lala.DTO.UserDTO;
import com.example.lala.Mapper.UserMapper;
import com.example.lala.Service.LmsService;
import com.example.lala.Service.LectureProgressService;
import com.example.lala.Service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lms")
public class LmsAPI extends BaseService {

    private final LmsService lmsService;
    private final LectureProgressService progressService;
    // 🔥 FileUploadService 의존성 주입 추가
    private final FileUploadService fileUploadService;
    private final UserMapper userMapper;

    // 최근 강의 정보 조회 (진도율 포함)
    @GetMapping("/myRecentLecture")
    public ResponseEntity<?> myRecentLecture() {
        try {
            log.info("[LmsAPI] 최근 강의 조회 요청: userId={}", getCurrentUserId());
            Map<String, Object> result = lmsService.myRecentLecture();
            log.info("[LmsAPI] 최궼 강의 조회 성공");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[LmsAPI] 최근 강의 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "강의 정보를 불러올 수 없습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * 🎯 특정 강의 정보 조회 (새로 추가)
     */
    @GetMapping("/lecture/{lectureId}")
    public ResponseEntity<?> getLectureInfo(@PathVariable String lectureId) {
        try {
            String userId = getCurrentUserId();
            
            log.info("[LmsAPI] 특정 강의 정보 조회: lectureId={}, userId={}", lectureId, userId);
            
            // 🔍 접근 권한 확인
            boolean hasAccess = lmsService.hasUserAccessToLecture(userId, lectureId);
            
            if (!hasAccess) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "해당 강의에 접근할 권한이 없습니다."));
            }
            
            // 🔍 강의 정보 조회
            Map<String, Object> lectureInfo = lmsService.getLectureInfoById(lectureId);
            
            if (lectureInfo == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "강의를 찾을 수 없습니다."));
            }
            
            // 🔍 사용자 정보 추가
            Map<String, Object> response = new HashMap<>();
            response.put("lecture", lectureInfo);
            response.put("userInfo", Map.of("accountType", getUserType(userId)));
            
            log.info("[LmsAPI] 특정 강의 정보 조회 성공: lectureId={}", lectureId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[LmsAPI] 특정 강의 정보 조회 실패 - lectureId: {}", lectureId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 타입 가져오기 유틸리티 메서드
     */
    private int getUserType(String userId) {
        try {
            // 🎯 실제 데이터베이스에서 사용자 정보 조회
            // 예시: 0 = 일반 사용자, 1 = 지식 제공자, 2 = 관리자
            // UserMapper를 통해 실제 user 테이블에서 account_type 조회
            
            // 현재는 기본값 0 반환 (일반 사용자)
            // 실제 구현 시에는 아래와 같이 구현해야 합니다:
             UserProfile user = userMapper.selectById(userId);
             return user != null ? user.getAccountType() : 0;

        } catch (Exception e) {
            log.error("[LmsAPI] 사용자 타입 조회 실패: userId={}", userId, e);
            return 0; // 기본값
        }
    }

    // 목차 조회
    @GetMapping("/myRecentLecture/chapters")
    public ResponseEntity<?> chapters() {
        try {
            log.info("[LmsAPI] 강의 목차 조회 요청: userId={}", getCurrentUserId());
            List<Chapter> chapters = lmsService.lectureChapters();
            log.info("[LmsAPI] 목차 조회 성공: {}개 목차", chapters.size());
            return ResponseEntity.ok(chapters);
        } catch (Exception e) {
            log.error("[LmsAPI] 목차 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "목차 정보를 불러올 수 없습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    // 자료 조회
    @GetMapping("/myRecentLecture/chapters/materials")
    public ResponseEntity<?> lectureMaterials() {
        try {
            log.info("[LmsAPI] 강의 자료 조회 요청: userId={}", getCurrentUserId());
            Map<String, Object> materials = lmsService.lectureMaterials();
            log.info("[LmsAPI] 자료 조회 성공");
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            log.error("[LmsAPI] 자료 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "자료 정보를 불러올 수 없습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    //  새로 추가된 목차별 자료 조회 API
    @GetMapping("/myRecentLecture/chapters/{chapterId}/materials")
    public ResponseEntity<?> getChapterMaterials(@PathVariable String chapterId) {
        try {
            log.info("[LmsAPI] 목차별 자료 조회: chapterId={}, userId={}", chapterId, getCurrentUserId());

            // LmsService에 목차별 자료 조회 메서드 추가 필요
            Map<String, Object> materials = lmsService.getChapterMaterials(chapterId);

            log.info("[LmsAPI] 목차별 자료 조회 성공: chapterId={}", chapterId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            log.error("[LmsAPI] 목차별 자료 조회 실패: chapterId={}, error={}", chapterId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "목차 자료를 불러올 수 없습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * 🎯 특정 강의의 목차 조회 (새로 추가)
     */
    @GetMapping("/lecture/{lectureId}/chapters")
    public ResponseEntity<?> getLectureChapters(@PathVariable String lectureId) {
        try {
            String userId = getCurrentUserId();
            
            log.info("[LmsAPI] 특정 강의 목차 조회: lectureId={}, userId={}", lectureId, userId);
            
            // 🔍 접근 권한 확인
            boolean hasAccess = lmsService.hasUserAccessToLecture(userId, lectureId);
            
            if (!hasAccess) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "해당 강의에 접근할 권한이 없습니다."));
            }
            
            // 🔍 목차 조회
            List<Chapter> chapters = lmsService.getLectureChaptersById(lectureId);
            
            log.info("[LmsAPI] 특정 강의 목차 조회 성공: lectureId={}, 목차수={}", lectureId, chapters.size());
            return ResponseEntity.ok(chapters);
            
        } catch (Exception e) {
            log.error("[LmsAPI] 특정 강의 목차 조회 실패 - lectureId: {}", lectureId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 🎯 특정 강의의 목차별 자료 조회 (새로 추가)
     */
    @GetMapping("/lecture/{lectureId}/chapters/{chapterId}/materials")
    public ResponseEntity<?> getLectureChapterMaterials(@PathVariable String lectureId, @PathVariable String chapterId) {
        try {
            String userId = getCurrentUserId();
            
            log.info("[LmsAPI] 특정 강의의 목차별 자료 조회: lectureId={}, chapterId={}, userId={}", lectureId, chapterId, userId);
            
            // 🔍 접근 권한 확인
            boolean hasAccess = lmsService.hasUserAccessToLecture(userId, lectureId);
            
            if (!hasAccess) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "해당 강의에 접근할 권한이 없습니다."));
            }
            
            // 🔍 목차별 자료 조회
            Map<String, Object> materials = lmsService.getChapterMaterials(chapterId);
            
            log.info("[LmsAPI] 특정 강의의 목차별 자료 조회 성공: lectureId={}, chapterId={}", lectureId, chapterId);
            return ResponseEntity.ok(materials);
            
        } catch (Exception e) {
            log.error("[LmsAPI] 특정 강의의 목차별 자료 조회 실패 - lectureId: {}, chapterId: {}", lectureId, chapterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    // 추가자료 상세 조회
    @GetMapping("/myRecentLecture/chapters/materials/addiDetail/{materialId}")
    public ResponseEntity<?> AddiDetail(@PathVariable String materialId) {
        try {
            log.info("[LmsAPI] 추가자료 상세 조회: materialId={}", materialId);
            AddiDetail detail = lmsService.addiDetail(materialId);

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "자료를 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("[LmsAPI] 추가자료 상세 조회 실패: materialId={}, error={}", materialId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "자료 상세 정보를 불러올 수 없습니다."));
        }
    }

    // 영상 상세 조회
    @GetMapping("/myRecentLecture/chapters/materials/videoDetail/{materialId}")
    public ResponseEntity<?> videoDetail(@PathVariable String materialId) {
        try {
            log.info("[LmsAPI] 영상 상세 조회: materialId={}", materialId);
            VideoDetail detail = lmsService.videoDetail(materialId);

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "영상을 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("[LmsAPI] 영상 상세 조회 실패: materialId={}, error={}", materialId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "영상 정보를 불러올 수 없습니다."));
        }
    }

    // 문제 상세 조회
    @GetMapping("/myRecentLecture/chapters/materials/questionDetail/{materialId}")
    public ResponseEntity<?> questionDetail(@PathVariable String materialId) {
        try {
            log.info("[LmsAPI] 문제 상세 조회: materialId={}", materialId);
            QuestionDetail detail = lmsService.questionDetail(materialId);

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "문제를 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("[LmsAPI] 문제 상세 조회 실패: materialId={}, error={}", materialId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "문제 정보를 불러올 수 없습니다."));
        }
    }

    // 댓글 조회
    @GetMapping("/myRecentLecture/chapters/materials/Detail/qna/{materialId}")
    public ResponseEntity<?> materialComments(@PathVariable String materialId) {
        try {
            log.info("[LmsAPI] 댓글 조회: materialId={}", materialId);
            List<Comment> comments = lmsService.materialComments(materialId);
            log.info("[LmsAPI] 댓글 조회 성공: {}개 댓글", comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("[LmsAPI] 댓글 조회 실패: materialId={}, error={}", materialId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글을 불러올 수 없습니다."));
        }
    }

    // 댓글 등록
    @PostMapping("/myRecentLecture/chapters/materials/questionDetail/regComment")
    public ResponseEntity<?> regQna(@RequestBody RegQna regQna) {
        try {
            log.info("[LmsAPI] 댓글 등록 요청: userId={}", getCurrentUserId());
            lmsService.regQna(regQna);
            log.info("[LmsAPI] 댓글 등록 성공");
            return ResponseEntity.ok(Map.of("success", true, "message", "댓글이 등록되었습니다."));
        } catch (Exception e) {
            log.error("[LmsAPI] 댓글 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 등록에 실패했습니다."));
        }
    }

    // 강의 취소
    @DeleteMapping("/deleteLecture/{lectureId}")
    public ResponseEntity<?> deleteLecture(@PathVariable String lectureId) {
        try {
            log.info("[LmsAPI] 강의 취소 요청: lectureId={}, userId={}", lectureId, getCurrentUserId());
            lmsService.deleteLecture(lectureId);
            log.info("[LmsAPI] 강의 취소 성공: lectureId={}", lectureId);
            return ResponseEntity.ok(Map.of("success", true, "message", "강의가 취소되었습니다."));
        } catch (Exception e) {
            log.error("[LmsAPI] 강의 취소 실패: lectureId={}, error={}", lectureId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "강의 취소에 실패했습니다."));
        }
    }

    // 🎯 수정된 강의 등록 API - JSON body로 interestedId를 함께 받음
    @PostMapping("/regLecture")
    public ResponseEntity<Map<String, Object>> regLecture(
            @RequestBody LectureRegistrationRequest request) {

        try {
            log.info("[LmsAPI] 강의 등록 요청: 강의명={}, 업로더={}, interestedId={}",
                    request.getName(), getCurrentUserId(), request.getInterestedId());

            // 1. 입력값 검증
            if (request.getInterestedId() == null || request.getInterestedId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "카테고리(interestedId)는 필수입니다."
                        ));
            }

            // 2. RegLecture 객체 생성
            RegLecture regLecture = request.toRegLecture();

            // 3. LectureField 객체 생성
            LectureField lectureField = request.toLectureField();

            // 4. 🎯 올바른 순서로 처리: lecture → lecture_field
            String lectureId = lmsService.regLecture(regLecture, lectureField);

            log.info("[LmsAPI] 강의 등록 성공: lectureId={}", lectureId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "lectureId", lectureId,
                    "message", "강의 등록 완료"
            ));

        } catch (IllegalArgumentException e) {
            log.warn("[LmsAPI] 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "잘못된 요청: " + e.getMessage(),
                            "errorType", "VALIDATION_ERROR"
                    ));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("[LmsAPI] 데이터베이스 제약조건 위반: {}", e.getMessage());
            String userMessage = "\ub370이터 제약조건 위반";
            if (e.getMessage().contains("foreign key constraint")) {
                userMessage = "참조된 데이터가 존재하지 않습니다. 카테고리 선택을 확인해주세요.";
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", userMessage,
                            "errorType", "DATABASE_CONSTRAINT_ERROR"
                    ));

        } catch (Exception e) {
            log.error("[LmsAPI] 강의 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "강의 등록 실패: " + e.getMessage()
                    ));
        }
    }

    // 목차 등록
    @PostMapping("/regChapter")
    public ResponseEntity<Map<String, String>> insertLectureChapter(@RequestBody RegChapter regChapter) {
        try {
            log.info("[LmsAPI] 목차 등록 요청: 목차명={}, lectureId={}", regChapter.getName(), regChapter.getLectureId());
            String chapterId = lmsService.insertLectureChapter(regChapter);
            log.info("[LmsAPI] 목차 등록 성공: chapterId={}", chapterId);
            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "chapterId", chapterId,
                    "message", "목차 등록 완료"
            ));
        } catch (Exception e) {
            log.error("[LmsAPI] 목차 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "목차 등록 실패: " + e.getMessage()
                    ));
        }
    }

    //  자료 타입 등록
    @PostMapping("/regMaterialType/{type}")
    public ResponseEntity<Map<String, String>> regMaterial(@PathVariable int type) {
        try {
            log.info("[LmsAPI] 자료 등록 요청: type={}", type);
            String materialId = lmsService.regMaterial(type);
            log.info("[LmsAPI] 자료 등록 성공: materialId={}", materialId);
            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "materialId", materialId,
                    "message", "자료 등록 완료"
            ));
        } catch (Exception e) {
            log.error("[LmsAPI] 자료 등록 실패: type={}, error={}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "자료 등록 실패: " + e.getMessage()
                    ));
        }
    }

    // 호환성 유지를 위한 메서드
    @GetMapping("/getMaterialId")
    public ResponseEntity<?> getMaterialId() {
        try {
            String materialId = lmsService.getMaterialId();
            return ResponseEntity.ok(materialId);
        } catch (Exception e) {
            log.error("[LmsAPI] materialId 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "자료 ID 조회 실패"));
        }
    }

    //   강의 내용 등록
    @PostMapping("/regContent")
    public ResponseEntity<?> regContent(@RequestBody RegContent regContent) {
        try {
            log.info("[LmsAPI] 강의 내용 등록: materialId={}", regContent.getLectureMaterialId());
            lmsService.regContent(regContent);
            return ResponseEntity.ok(Map.of("success", true, "message", "강의 내용 등록 완료"));
        } catch (Exception e) {
            log.error("[LmsAPI] 강의 내용 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "강의 내용 등록 실패"));
        }
    }

    //   추가 자료 등록
    @PostMapping("/regAddi")
    public ResponseEntity<?> regAddi(@RequestBody RegAddiMaterial regAddiMaterial) {
        try {
            log.info("[LmsAPI] 추가자료 등록: materialId={}", regAddiMaterial.getAdditionalFileId());
            lmsService.regAddi(regAddiMaterial);
            return ResponseEntity.ok(Map.of("success", true, "message", "추가자료 등록 완료"));
        } catch (Exception e) {
            log.error("[LmsAPI] 추가자료 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "추가자료 등록 실패"));
        }
    }

    //   영상 자료 등록
    @PostMapping("/regVideo")
    public ResponseEntity<?> regVideo(@RequestBody RegVideoMaterial regVideoMaterial) {
        try {
            log.info("[LmsAPI] 영상자료 등록: materialId={}", regVideoMaterial.getLectureMaterialId());
            lmsService.regVideo(regVideoMaterial);
            return ResponseEntity.ok(Map.of("success", true, "message", "영상자료 등록 완료"));
        } catch (Exception e) {
            log.error("[LmsAPI] 영상자료 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "영상자료 등록 실패"));
        }
    }

    //   문제 자료 등록
    @PostMapping("/regQuestion")
    public ResponseEntity<?> regQuestionMat(@RequestBody RegQuestionMaterial regQuestionMaterial) {
        try {
            log.info("[LmsAPI] 문제자료 등록: materialId={}", regQuestionMaterial.getLectureMaterialId());
            lmsService.regQuestionMat(regQuestionMaterial);
            return ResponseEntity.ok(Map.of("success", true, "message", "문제자료 등록 완료"));
        } catch (Exception e) {
            log.error("[LmsAPI] 문제자료 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "문제자료 등록 실패"));
        }
    }

    //   문제 등록
    @PostMapping("/regQuestionForReal")
    public ResponseEntity<?> regQuestion(@RequestBody RegQuestion question) {
        try {
            log.info("[LmsAPI] 문제 등록: question={}", question.getQuestion());
            lmsService.regQuestion(question);
            return ResponseEntity.ok(Map.of("success", true, "message", "문제 등록 완료"));
        } catch (Exception e) {
            log.error("[LmsAPI] 문제 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "문제 등록 실패"));
        }
    }

    // 📊 진도율 관련 API들

    /**
     * 진도율 업데이트
     */
    @PostMapping("/updateProgress/{lectureId}/{materialId}")
    public ResponseEntity<Map<String, Object>> updateMaterialProgress(
            @PathVariable String lectureId,
            @PathVariable String materialId) {

        try {
            String userId = getCurrentUserId();
            log.info("[LmsAPI] 진도율 업데이트 요청: userId={}, lectureId={}, materialId={}",
                    userId, lectureId, materialId);

            progressService.updateMaterialProgress(userId, lectureId, materialId);

            log.info("[LmsAPI] 진도율 업데이트 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "진도율이 업데이트되었습니다"
            ));

        } catch (RuntimeException e) {
            log.warn("[LmsAPI] 진도율 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[LmsAPI] 진도율 업데이트 시스템 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "진도율 업데이트 중 오류가 발생했습니다"
                    ));
        }
    }

    /**
     * 현재 진도율 조회
     */
    @GetMapping("/getProgress/{lectureId}")
    public ResponseEntity<?> getCurrentProgress(@PathVariable String lectureId) {
        try {
            String userId = getCurrentUserId();
            log.info("[LmsAPI] 진도율 조회 요청: userId={}, lectureId={}", userId, lectureId);

            // 입력값 검증
            if (lectureId == null || lectureId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "강의 ID가 필요합니다.",
                                "progressPercentage", 0,
                                "userType", "unknown"
                        ));
            }

            // 사용자 수강 권한 확인
            if (!progressService.hasUserAccess(userId, lectureId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", "수강 권한이 없는 강의입니다.",
                                "progressPercentage", 0,
                                "userType", "unknown"
                        ));
            }

            // 진도율 정보 조회
            Map<String, Object> progressInfo = progressService.getLMSPageInfo(userId, lectureId);

            if (progressInfo == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "success", false,
                                "message", "진도율 정보를 불러올 수 없습니다.",
                                "progressPercentage", 0,
                                "userType", "student"
                        ));
            }

            log.info("[LmsAPI] 진도율 조회 성공: progressPercentage={}",
                    progressInfo.get("progressPercentage"));
            return ResponseEntity.ok(progressInfo);

        } catch (Exception e) {
            log.error("[LmsAPI] 진도율 조회 실패: lectureId={}, error={}", lectureId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "진도율 조회에 실패했습니다",
                            "progressPercentage", 0,
                            "userType", "unknown"
                    ));
        }
    }

    // 🖼️ **S3 파일 업로드 API들** 🖼️

    /**
     * 🖼️ 강의 썸네일 업로드 API
     */
    @PostMapping("/upload/thumbnail")
    public ResponseEntity<Map<String, Object>> uploadThumbnail(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("[LmsAPI] 썸네일 업로드 요청: 파일명={}, 사용자={}",
                    file.getOriginalFilename(), getCurrentUserId());

            // 1. 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "파일이 선택되지 않았습니다."));
            }

            // 2. 이미지 파일인지 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일만 업로드 가능합니다."));
            }

            // 3. S3에 업로드
            String thumbnailUrl = fileUploadService.uploadImageFile(file);

            // 4. 성공 응답
            response.put("success", true);
            response.put("message", "썸네일 업로드 완료");
            response.put("url", thumbnailUrl);
            response.put("originalName", file.getOriginalFilename());

            log.info("[LmsAPI] 썸네일 업로드 성공: URL={}", thumbnailUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[LmsAPI] 썸네일 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "썸네일 업로드 실패: " + e.getMessage()
                    ));
        }
    }

    /**
     * 🎬 강의 영상 업로드 API
     */
    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("[LmsAPI] 영상 업로드 요청: 파일명={}, 크기={}MB, 사용자={}",
                    file.getOriginalFilename(), file.getSize() / (1024 * 1024), getCurrentUserId());

            // 1. 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "파일이 선택되지 않았습니다."));
            }

            // 2. 비디오 파일인지 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비디오 파일만 업로드 가능합니다."));
            }

            // 3. 파일 크기 검증 (500MB 제한)
            if (file.getSize() > 500 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비디오 파일은 500MB 이하만 업로드 가능합니다."));
            }

            // 4. S3에 업로드
            String videoUrl = fileUploadService.uploadVideoFile(file);

            // 5. 성공 응답
            response.put("success", true);
            response.put("message", "영상 업로드 완료");
            response.put("url", videoUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());

            log.info("[LmsAPI] 영상 업로드 성공: URL={}", videoUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[LmsAPI] 영상 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "영상 업로드 실패: " + e.getMessage()
                    ));
        }
    }

    /**
     * 📄 강의 자료 파일 업로드 API
     */
    @PostMapping("/upload/material")
    public ResponseEntity<Map<String, Object>> uploadMaterial(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("[LmsAPI] 자료 업로드 요청: 파일명={}, 크기={}KB, 사용자={}",
                    file.getOriginalFilename(), file.getSize() / 1024, getCurrentUserId());

            // 1. 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "파일이 선택되지 않았습니다."));
            }

            // 2. 파일 크기 검증 (100MB 제한)
            if (file.getSize() > 100 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "파일은 100MB 이하만 업로드 가능합니다."));
            }

            // 3. S3에 업로드
            String materialUrl = fileUploadService.uploadGeneralFile(file);

            // 4. 성공 응답
            response.put("success", true);
            response.put("message", "자료 업로드 완료");
            response.put("url", materialUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            log.info("[LmsAPI] 자료 업로드 성공: URL={}", materialUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[LmsAPI] 자료 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "자료 업로드 실패: " + e.getMessage()
                    ));
        }
    }



    /**
     * 전체 강의 생성 API
     */
    @PostMapping("/createCompleteLecture")
    public ResponseEntity<Map<String, Object>> createCompleteLecture(
            @RequestBody LectureCreationRequest request) {

        try {



            log.info("request : {}",request);

            log.info("[LmsAPI] 전체 강의 생성 요청: 업로더={}, 강의명={}",
                    getCurrentUserId(),
                    request.getLectureInfo() != null ? request.getLectureInfo().getName() : "null");
            log.info("[LmsAPI] 목차 수: {}",
                    request.getChapters() != null ? request.getChapters().size() : 0);


            log.info("test request: " + request);

            Map<String, Object> result = lmsService.createCompleteLecture(request);

            log.info("[LmsAPI] 전체 강의 생성 성공: lectureId={}", result.get("lectureId"));
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("[LmsAPI] 전체 강의 생성 실패: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "강의 생성 실패: " + e.getMessage(),
                    "error", e.getClass().getSimpleName(),
                    "timestamp", java.time.LocalDateTime.now().toString()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        } catch (Exception e) {
            log.error("[LmsAPI] 전체 강의 생성 시스템 오류: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "시스템 오류가 발생했습니다",
                    "error", "SystemError",
                    "timestamp", java.time.LocalDateTime.now().toString()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    // 퀴즈 등록
    @PostMapping("/submit/question")
    public ResponseEntity<?> submitQuestion(@RequestBody SubQuestion subQuestion) {
        try {
            String userId = getCurrentUserId();
            subQuestion.setUserId(userId);

            log.info("[퀴즈 제출] materialId={}, userId={}, answer={}",
                    subQuestion.getMaterialId(), userId, subQuestion.getAnswer());

            lmsService.subQuestion(subQuestion);
            return ResponseEntity.ok().body("ok");
        } catch (Exception e) {
            log.error("[퀴즈 제출 실패]", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    // 마지막 제출 답변 불러오기
    @GetMapping("/submitInfo/{materialId}/{userId}")
    public ResponseEntity<?> getSubmittedAnswer(@PathVariable String materialId,
                                                @PathVariable String userId) {
        log.info("[LmsAPI] 제출 정보 조회: materialId={}, userId={}", materialId, userId);
        SubmittedAnswer answer = lmsService.getSubmittedAnswer(materialId, userId);

        if (answer == null) {
            return ResponseEntity.ok(Map.of("submitted", false));
        }

        return ResponseEntity.ok(Map.of(
                "submitted", true,
                "answer", answer.getAnswer(),
                "submitDate", answer.getSubmitDate()
        ));
    }
    
    // 🎯 강의 수정 관련 API
    
    /**
     * 강의 정보 조회 (수정용)
     */
    @GetMapping("/edit/{lectureId}/info")
    public ResponseEntity<?> getLectureForEdit(@PathVariable String lectureId) {
        try {
            log.info("[LmsAPI] 강의 수정 정보 조회: lectureId={}, userId={}", lectureId, getCurrentUserId());
            
            Map<String, Object> lectureInfo = lmsService.getLectureForEdit(lectureId);
            
            log.info("[LmsAPI] 강의 수정 정보 조회 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "lecture", lectureInfo
            ));
            
        } catch (RuntimeException e) {
            log.warn("[LmsAPI] 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[LmsAPI] 강의 수정 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "강의 정보를 불러올 수 없습니다."
                    ));
        }
    }
    
    /**
     * 강의 정보 수정
     */
    @PutMapping("/edit/{lectureId}")
    public ResponseEntity<?> updateLecture(@PathVariable String lectureId,
                                           @RequestBody LectureUpdateRequest updateRequest) {
        try {
            log.info("[LmsAPI] 강의 정보 수정 요청: lectureId={}, userId={}", lectureId, getCurrentUserId());
            
            lmsService.updateLecture(lectureId, updateRequest);
            
            log.info("[LmsAPI] 강의 정보 수정 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "강의 정보가 수정되었습니다."
            ));
            
        } catch (RuntimeException e) {
            log.warn("[LmsAPI] 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[LmsAPI] 강의 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "강의 수정에 실패했습니다."
                    ));
        }
    }
    
    /**
     * 목차 수정
     */
    @PutMapping("/edit/chapter/{chapterId}")
    public ResponseEntity<?> updateChapter(@PathVariable String chapterId,
                                           @RequestBody RegChapter updateRequest) {
        try {
            log.info("[LmsAPI] 목차 수정 요청: chapterId={}, userId={}", chapterId, getCurrentUserId());
            
            lmsService.updateChapter(chapterId, updateRequest);
            
            log.info("[LmsAPI] 목차 수정 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "목차가 수정되었습니다."
            ));
            
        } catch (RuntimeException e) {
            log.warn("[LmsAPI] 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[LmsAPI] 목차 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "목차 수정에 실패했습니다."
                    ));
        }
    }
    
    /**
     * 목차 삭제
     */
    @DeleteMapping("/edit/chapter/{chapterId}")
    public ResponseEntity<?> deleteChapter(@PathVariable String chapterId) {
        try {
            log.info("[LmsAPI] 목차 삭제 요청: chapterId={}, userId={}", chapterId, getCurrentUserId());
            
            lmsService.deleteChapter(chapterId);
            
            log.info("[LmsAPI] 목차 삭제 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "목차가 삭제되었습니다."
            ));
            
        } catch (RuntimeException e) {
            log.warn("[LmsAPI] 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[LmsAPI] 목차 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "목차 삭제에 실패했습니다."
                    ));
        }
    }
}