package com.example.lala.Controller;

import com.example.lala.Service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 📁 파일 업로드 컨트롤러
 *
 * 이 컨트롤러는 강의 썸네일 이미지와 기타 파일들을 업로드하는 기능을 담당합니다.
 * 초급 개발자도 이해하기 쉽게 작성되었습니다.
 *
 * 주요 기능:
 * 1. 이미지 파일 업로드 (썸네일용)
 * 2. 일반 파일 업로드 (강의 자료용)
 * 3. 파일 검증 및 에러 처리
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 🖼️ 이미지 파일 업로드 API
     * 강의 썸네일 등 이미지 파일을 업로드할 때 사용합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일의 URL과 처리 결과
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("🖼️ 이미지 업로드 요청: 파일명={}, 크기={}KB",
                    file.getOriginalFilename(), file.getSize() / 1024);

            // 1. 파일 기본 검증
            if (file.isEmpty()) {
                return createErrorResponse("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST);
            }

            // 2. 이미지 파일인지 검증
            if (!isImageFile(file)) {
                return createErrorResponse("이미지 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
            }

            // 3. 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                return createErrorResponse("파일 크기는 5MB 이하만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
            }

            // 4. 파일 업로드 실행
            String fileUrl = fileUploadService.uploadImageFile(file);

            // 5. 성공 응답 생성
            response.put("success", true);
            response.put("message", "이미지 업로드 성공");
            response.put("url", fileUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());

            log.info("✅ 이미지 업로드 성공: URL={}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 이미지 업로드 실패: {}", e.getMessage(), e);
            return createErrorResponse("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 📄 일반 파일 업로드 API
     * 강의 자료 등 일반 파일을 업로드할 때 사용합니다.
     *
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL과 처리 결과
     */
    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("📄 파일 업로드 요청: 파일명={}, 크기={}KB",
                    file.getOriginalFilename(), file.getSize() / 1024);

            // 1. 파일 기본 검증
            if (file.isEmpty()) {
                return createErrorResponse("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST);
            }

            // 2. 파일 크기 검증 (100MB 제한)
            if (file.getSize() > 100 * 1024 * 1024) {
                return createErrorResponse("파일 크기는 100MB 이하만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
            }

            // 3. 위험한 파일 형식 검증
            if (isDangerousFile(file)) {
                return createErrorResponse("보안상 업로드할 수 없는 파일 형식입니다.", HttpStatus.BAD_REQUEST);
            }

            // 4. 파일 업로드 실행
            String fileUrl = fileUploadService.uploadGeneralFile(file);

            // 5. 성공 응답 생성
            response.put("success", true);
            response.put("message", "파일 업로드 성공");
            response.put("url", fileUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            log.info("✅ 파일 업로드 성공: URL={}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 파일 업로드 실패: {}", e.getMessage(), e);
            return createErrorResponse("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 🎬 비디오 파일 업로드 API
     * 강의 동영상 파일을 업로드할 때 사용합니다.
     *
     * @param file 업로드할 비디오 파일
     * @return 업로드된 파일의 URL과 처리 결과
     */
    @PostMapping("/video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("🎬 비디오 업로드 요청: 파일명={}, 크기={}MB",
                    file.getOriginalFilename(), file.getSize() / (1024 * 1024));

            // 1. 파일 기본 검증
            if (file.isEmpty()) {
                return createErrorResponse("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST);
            }

            // 2. 비디오 파일인지 검증
            if (!isVideoFile(file)) {
                return createErrorResponse("비디오 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
            }

            // 3. 파일 크기 검증 (500MB 제한)
            if (file.getSize() > 500 * 1024 * 1024) {
                return createErrorResponse("비디오 파일 크기는 500MB 이하만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
            }

            // 4. 파일 업로드 실행
            String fileUrl = fileUploadService.uploadVideoFile(file);

            // 5. 성공 응답 생성
            response.put("success", true);
            response.put("message", "비디오 업로드 성공");
            response.put("url", fileUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("duration", "미구현"); // TODO: 나중에 비디오 길이 추출 기능 추가

            log.info("✅ 비디오 업로드 성공: URL={}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 비디오 업로드 실패: {}", e.getMessage(), e);
            return createErrorResponse("비디오 업로드 중 오류가 발생했습니다: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 🔧 헬퍼 메서드들 ====================

    /**
     * 🖼️ 이미지 파일인지 검증하는 함수
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 🎬 비디오 파일인지 검증하는 함수
     */
    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        return contentType.startsWith("video/") ||
                contentType.equals("application/mp4") ||
                contentType.equals("video/mp4") ||
                contentType.equals("video/avi") ||
                contentType.equals("video/mov") ||
                contentType.equals("video/wmv");
    }

    /**
     * 🚫 위험한 파일인지 검증하는 함수
     */
    private boolean isDangerousFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return true;

        String extension = getFileExtension(filename).toLowerCase();

        // 위험한 파일 확장자 목록
        String[] dangerousExtensions = {
                "exe", "bat", "cmd", "com", "scr", "pif", "jar", "js", "vbs", "ps1"
        };

        for (String dangerous : dangerousExtensions) {
            if (extension.equals(dangerous)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 📄 파일 확장자를 추출하는 함수
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * ❌ 에러 응답을 생성하는 함수
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * 📊 업로드 상태 조회 API
     * 큰 파일 업로드 시 진행 상황을 확인할 때 사용합니다. (추후 구현 예정)
     */
    @GetMapping("/status/{uploadId}")
    public ResponseEntity<Map<String, Object>> getUploadStatus(@PathVariable String uploadId) {
        Map<String, Object> response = new HashMap<>();

        // TODO: 실제 업로드 상태 조회 로직 구현
        response.put("success", true);
        response.put("uploadId", uploadId);
        response.put("status", "completed");
        response.put("progress", 100);

        return ResponseEntity.ok(response);
    }
}