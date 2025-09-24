package com.example.lala.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.lala.Config.BaseService;
import com.example.lala.Mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일 업로드 서비스
 * <p>
 * AWS S3를 이용한 프로필 이미지 업로드 기능을 제공합니다.
 * 사용자별로 고정된 파일명을 사용하되, 원본 파일 타입을 유지하여
 * 자동 덮어쓰기를 지원합니다.
 * <p>
 * 주요 기능:
 * - 이미지 파일 유효성 검증 (타입, 크기)
 * - AWS S3 업로드 (기존 파일 자동 덮어쓰기)
 * - 사용자별 고정 파일명 + 원본 타입 유지
 * - 업로드된 파일의 공개 URL 반환
 * - 데이터베이스 프로필 이미지 URL 업데이트
 */
@Service
@Slf4j
public class FileUploadService extends BaseService {

    private final UserMapper userMapper;
    private final AmazonS3Client amazonS3Client;

    // s3 버킷 주소 변수에 할당해놓고
    private static final String THUMBNAIL_PATH_PREFIX = "lecture-thumbnails/";
    private static final String VIDEO_PATH_PREFIX = "lecture-videos/";
    private static final String DOCS_PATH_PREFIX = "lecture-docs/";


    // AWS S3 버킷명
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 업로드 가능한 최대 파일 크기 (5MB)
    @Value("${app.profile-image.max-size:5242880}")
    private long maxFileSize;

    // 허용되는 이미지 파일 타입 목록
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png"
    };

    // S3 업로드 경로 prefix
    private static final String UPLOAD_PATH_PREFIX = "profile-images/";

    /**
     * FileUploadService 생성자
     *
     * @param amazonS3Client AWS S3 클라이언트 인스턴스
     * @param userMapper     사용자 정보 매퍼
     */
    public FileUploadService(AmazonS3Client amazonS3Client, UserMapper userMapper) {
        this.amazonS3Client = amazonS3Client;
        this.userMapper = userMapper;
    }

    /**
     * 프로필 이미지를 AWS S3에 업로드합니다.
     * <p>
     * 업로드 특징:
     * - 사용자별 고정 파일명 사용 (userId 기반)
     * - 원본 파일 타입(확장자) 유지
     * - 기존 파일이 있으면 자동으로 덮어쓰기
     * - PNG 투명도 보존, JPEG 품질 유지
     * - 데이터베이스의 프로필 이미지 URL 자동 업데이트
     *
     * @param file 업로드할 이미지 파일
     * @return String 업로드된 파일의 공개 URL
     * @throws IOException              파일 업로드 중 I/O 에러 발생 시
     * @throws IllegalArgumentException 파일 유효성 검증 실패 시
     */
    @Transactional
    public String saveProfileImage(MultipartFile file) throws IOException {
        // 1. 파일 유효성 검증
        validateFile(file);

        // 2. 현재 사용자 ID 획득
        String userId = getCurrentUserId();
        log.info("프로필 이미지 업로드 시작 - 사용자 ID: {}", userId);

        try {
            // 3. 기존 프로필 이미지 파일들 정리 (다른 확장자로 업로드된 파일 삭제)
            cleanupExistingProfileImages(userId);

            // 4. 원본 파일 타입에 맞는 파일 키 생성
            String uploadKey = generateProfileImageKey(userId, file.getContentType());
            log.debug("생성된 S3 키: {}", uploadKey);

            // 5. S3 업로드용 메타데이터 설정 (원본 타입 유지)
            ObjectMetadata metadata = createOptimizedObjectMetadata(file);

            // 6. S3에 파일 업로드 (기존 파일이 있으면 자동 덮어쓰기)
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);
            log.info("S3 업로드 완료: {}", uploadKey);

            // 7. 업로드된 파일의 공개 URL 생성
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String imageUrl = publicUrl.toString();
            log.info("생성된 공개 URL: {}", imageUrl);

            // 8. 데이터베이스의 프로필 이미지 URL 업데이트
            int updateResult = updateUserProfileImageUrl(userId, imageUrl);

            if (updateResult == 0) {
                log.error("DB 업데이트 실패 - 영향받은 행 수: 0, userId: {}", userId);
                throw new RuntimeException("데이터베이스 업데이트에 실패했습니다. 사용자 정보를 확인해주세요.");
            }

            log.info("프로필 이미지 업로드 완료 - 사용자 ID: {}, URL: {}", userId, imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage(), e);
            throw new IOException("프로필 이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 프로필 이미지 키를 원본 파일 타입에 맞게 생성합니다.
     * <p>
     * 키 구조: profile-images/{userId}.{extension}
     * - JPEG/JPG: .jpg
     * - PNG: .png
     *
     * @param userId      사용자 ID
     * @param contentType 원본 파일의 Content-Type
     * @return String S3 객체 키
     */
    private String generateProfileImageKey(String userId, String contentType) {
        String extension = getFileExtensionFromContentType(contentType);
        return UPLOAD_PATH_PREFIX + userId + extension;
    }

    /**
     * Content-Type에서 적절한 파일 확장자를 추출합니다.
     *
     * @param contentType 파일의 Content-Type
     * @return String 파일 확장자 (.jpg, .png)
     */
    private String getFileExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return ".jpg"; // 기본값
        }

        switch (contentType.toLowerCase()) {
            case "image/png":
                return ".png";
            case "image/jpeg":
            case "image/jpg":
            default:
                return ".jpg";
        }
    }

    /**
     * 사용자의 기존 프로필 이미지 파일들을 정리합니다.
     * <p>
     * 새로운 파일을 업로드할 때 다른 확장자로 저장된
     * 기존 프로필 이미지들을 삭제하여 중복을 방지합니다.
     * <p>
     * 예: PNG 파일 업로드 시 기존 JPG 파일 삭제
     *
     * @param userId 사용자 ID
     */
    private void cleanupExistingProfileImages(String userId) {
        String[] extensions = {".jpg", ".png"};

        for (String ext : extensions) {
            String existingKey = UPLOAD_PATH_PREFIX + userId + ext;
            try {
                // 파일이 존재하는지 확인
                amazonS3Client.getObjectMetadata(bucket, existingKey);
                // 존재하면 삭제
                amazonS3Client.deleteObject(bucket, existingKey);
                log.debug("기존 프로필 이미지 삭제: {}", existingKey);
            } catch (Exception e) {
                // 파일이 없거나 접근 불가한 경우 무시
                log.debug("기존 파일 없음 또는 삭제 실패: {}", existingKey);
            }
        }
    }

    /**
     * 데이터베이스의 사용자 프로필 이미지 URL을 업데이트합니다.
     *
     * @param userId          사용자 ID
     * @param profileImageUrl 새로운 프로필 이미지 URL
     * @return int 업데이트된 행의 수
     */
    private int updateUserProfileImageUrl(String userId, String profileImageUrl) {
        try {
            // 현재 사용자 정보 확인
            log.debug("사용자 프로필 업데이트 시도 - userId: {}, url: {}", userId, profileImageUrl);

            // HashMap을 사용해서 명확한 파라미터 전달
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("profileImageUrl", profileImageUrl);

            // 업데이트 실행
            int result = userMapper.updateProfileImage(params);

            log.info("프로필 이미지 URL 업데이트 결과 - userId: {}, 영향받은 행 수: {}", userId, result);

            return result;

        } catch (Exception e) {
            log.error("프로필 이미지 URL 업데이트 실패 - userId: {}, url: {}, 오류: {}",
                    userId, profileImageUrl, e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 URL 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 업로드할 파일의 유효성을 검증합니다.
     * <p>
     * 검증 항목:
     * - 파일 존재 여부
     * - 파일 크기 제한 (5MB)
     * - 파일 타입 (JPEG, JPG, PNG만 허용)
     *
     * @param file 검증할 파일
     * @throws IllegalArgumentException 유효성 검증 실패 시
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("JPG, PNG 형식의 이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 파일의 Content-Type이 허용된 이미지 타입인지 확인합니다.
     *
     * @param contentType 확인할 Content-Type
     * @return boolean 허용된 이미지 타입인 경우 true, 아니면 false
     */
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return Arrays.stream(ALLOWED_CONTENT_TYPES)
                .anyMatch(allowedType -> allowedType.equalsIgnoreCase(contentType));
    }

    /**
     * 원본 파일 타입에 맞는 최적화된 S3 업로드용 ObjectMetadata를 생성합니다.
     * <p>
     * 설정 항목:
     * - Content-Type: 원본 파일 타입 유지
     * - Content-Length: 업로드 성능 최적화
     * - Cache-Control: 브라우저 캐싱 설정 (1시간)
     * - Content-Disposition: 인라인 표시 설정
     *
     * @param file 메타데이터를 생성할 파일
     * @return ObjectMetadata 최적화된 S3 업로드용 메타데이터
     */
    private ObjectMetadata createOptimizedObjectMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();

        // 원본 파일의 Content-Type 유지 (PNG 투명도 보존)
        metadata.setContentType(file.getContentType());

        // 파일 크기 설정 (업로드 성능 최적화)
        metadata.setContentLength(file.getSize());

        // 브라우저에서 인라인으로 표시되도록 설정
        metadata.setContentDisposition("inline");


        return metadata;
    }

    // ==================== 🎓 강의 관련 파일 업로드 메서드들 ====================

    /**
     * 🖼️ 강의 썸네일 이미지를 업로드합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return String 업로드된 파일의 공개 URL
     * @throws IOException 파일 업로드 중 오류 발생 시
     */
    /**
     * 🖼️ 강의 썸네일 이미지를 업로드합니다.
     */
    public String uploadImageFile(MultipartFile file) throws IOException {
        log.info("🖼️ 강의 썸네일 업로드 시작: {}", file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            validateImageFile(file);

            // 2. 고유한 파일명 생성
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String uploadKey = THUMBNAIL_PATH_PREFIX + fileName; // lecture-thumbnails/ 경로 사용

            // 3. S3 업로드용 메타데이터 생성
            ObjectMetadata metadata = createOptimizedObjectMetadata(file);

            // 4. S3에 파일 업로드
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);

            // 5. 공개 URL 생성 및 반환
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String imageUrl = publicUrl.toString();

            log.info("✅ 썸네일 업로드 성공: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("❌ 썸네일 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("썸네일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 📄 일반 파일(강의 자료)을 업로드합니다.
     *
     * @param file 업로드할 파일
     * @return String 업로드된 파일의 공개 URL
     * @throws IOException 파일 업로드 중 오류 발생 시
     */
    /* public String uploadGeneralFile(MultipartFile file) throws IOException {
        log.info("📄 강의 자료 업로드 시작: {}", file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            validateGeneralFile(file);

            // 2. 고유한 파일명 생성
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String uploadKey = DOCS_PATH_PREFIX + fileName; // lecture-docs/ 경로 사용

            // 3. S3 업로드용 메타데이터 생성
            ObjectMetadata metadata = createGeneralFileMetadata(file);

            // 4. S3에 파일 업로드
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);

            // 5. 공개 URL 생성 및 반환
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String fileUrl = publicUrl.toString();

            log.info("✅ 강의 자료 업로드 성공: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("❌ 강의 자료 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("강의 자료 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }
*/
    public String uploadGeneralFile(MultipartFile file) throws IOException {
        log.info("📄 강의 자료 업로드 시작: {}", file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            validateGeneralFile(file);

            // 2. 고유한 파일명 생성 (한글 등 특수문자 인코딩)
            String rawFileName = generateUniqueFileName(file.getOriginalFilename());
            String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8);
            String uploadKey = DOCS_PATH_PREFIX + encodedFileName; // lecture-docs/ 경로 사용

            // 3. 메타데이터 생성
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());

            // Content-Type 안전 처리
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream"; // fallback
            }
            metadata.setContentType(contentType);

            // 4. S3 업로드
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);

            // 5. URL 생성 및 반환
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String fileUrl = publicUrl.toString();

            log.info("✅ 강의 자료 업로드 성공: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("❌ 강의 자료 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("강의 자료 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 🎬 비디오 파일(강의 영상)을 업로드합니다.
     */
    public String uploadVideoFile(MultipartFile file) throws IOException {
        log.info("🎬 강의 영상 업로드 시작: {}", file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            validateVideoFile(file);

            // 2. 고유한 파일명 생성
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String uploadKey = VIDEO_PATH_PREFIX + fileName; // lecture-videos/ 경로 사용

            // 3. S3 업로드용 메타데이터 생성
            ObjectMetadata metadata = createVideoFileMetadata(file);

            // 4. S3에 파일 업로드
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);

            // 5. 공개 URL 생성 및 반환
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String videoUrl = publicUrl.toString();

            log.info("✅ 강의 영상 업로드 성공: {}", videoUrl);
            return videoUrl;

        } catch (Exception e) {
            log.error("❌ 강의 영상 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("강의 영상 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // ==================== 🔧 강의용 파일 검증 메서드들 ====================

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 파일 크기 검증 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 이미지 파일 타입 검증
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("JPG, PNG 형식의 이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 일반 파일 유효성 검증
     */
    private void validateGeneralFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 파일 크기 검증 (100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }

        // 위험한 파일 형식 검증
        if (isDangerousFileType(file)) {
            throw new IllegalArgumentException("보안상 업로드할 수 없는 파일 형식입니다.");
        }
    }

    /**
     * 비디오 파일 유효성 검증
     */
    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 파일 크기 검증 (500MB)
        if (file.getSize() > 500 * 1024 * 1024) {
            throw new IllegalArgumentException("비디오 파일 크기는 500MB를 초과할 수 없습니다.");
        }

        // 비디오 파일 타입 검증
        String contentType = file.getContentType();
        if (!isValidVideoType(contentType)) {
            throw new IllegalArgumentException("MP4, AVI, MOV 형식의 비디오 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 비디오 파일 타입 검증
     */
    private boolean isValidVideoType(String contentType) {
        if (contentType == null) return false;

        return contentType.equals("video/mp4") ||
                contentType.equals("video/avi") ||
                contentType.equals("video/quicktime") || // .mov
                contentType.equals("video/x-msvideo"); // .avi
    }

    /**
     * 위험한 파일 타입 검증
     */
    private boolean isDangerousFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return true;

        String extension = getFileExtension(filename).toLowerCase();
        String[] dangerousExtensions = {
                "exe", "bat", "cmd", "com", "scr", "pif", "jar", "js", "vbs", "ps1"
        };

        return Arrays.asList(dangerousExtensions).contains(extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userId = getCurrentUserId();
        String extension = getFileExtension(originalFilename);

        return userId + "_" + timestamp + "." + extension;
    }

    /**
     * 일반 파일용 메타데이터 생성
     */
    private ObjectMetadata createGeneralFileMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setContentDisposition("attachment; filename=\"" + file.getOriginalFilename() + "\"");
        return metadata;
    }

    /**
     * 비디오 파일용 메타데이터 생성
     */
    private ObjectMetadata createVideoFileMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setContentDisposition("inline");
        return metadata;
    }

    /**
     * 학력/약력 첨부파일을 AWS S3에 업로드합니다.
     * <p>
     * 업로드 특징:
     * - 사용자별 고정 파일명 사용 (userId_history_{index} 기반)
     * - 원본 파일 타입(확장자) 유지
     * - 기존 파일이 있으면 자동으로 덮어쓰기
     * - 다양한 파일 타입 지원 (PDF, 이미지, 문서 등)
     * - 공개 URL 반환
     *
     * @param file         업로드할 첨부파일
     * @param userId       사용자 ID
     * @param historyIndex 학력/약력 인덱스
     * @return String 업로드된 파일의 공개 URL
     * @throws IOException              파일 업로드 중 I/O 에러 발생 시
     * @throws IllegalArgumentException 파일 유효성 검증 실패 시
     */
    @Transactional
    public String saveHistoryAttachment(MultipartFile file, String userId, int historyIndex) throws IOException {
        // 1. 파일 유효성 검증
        validateHistoryFile(file);

        log.info("학력/약력 첨부파일 업로드 시작 - 사용자 ID: {}, 인덱스: {}", userId, historyIndex);

        try {
            // 2. 원본 파일 타입에 맞는 파일 키 생성
            String uploadKey = generateHistoryAttachmentKey(userId, historyIndex, file.getContentType());
            log.debug("생성된 S3 키: {}", uploadKey);

            // 3. S3 업로드용 메타데이터 설정 (원본 타입 유지)
            ObjectMetadata metadata = createOptimizedObjectMetadata(file);

            // 4. S3에 파일 업로드 (기존 파일이 있으면 자동 덮어쓰기)
            amazonS3Client.putObject(bucket, uploadKey, file.getInputStream(), metadata);
            log.info("S3 업로드 완료: {}", uploadKey);

            // 5. 업로드된 파일의 공개 URL 생성
            URL publicUrl = amazonS3Client.getUrl(bucket, uploadKey);
            String fileUrl = publicUrl.toString();
            log.info("생성된 공개 URL: {}", fileUrl);

            log.info("학력/약력 첨부파일 업로드 완료 - 사용자 ID: {}, 인덱스: {}, URL: {}", userId, historyIndex, fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("학력/약력 첨부파일 업로드 실패 - 사용자 ID: {}, 인덱스: {}, 오류: {}", userId, historyIndex, e.getMessage(), e);
            throw new IOException("학력/약력 첨부파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 학력/약력 첨부파일 키를 원본 파일 타입에 맞게 생성합니다.
     * <p>
     * 키 구조: history-attachments/{userId}_history_{index}.{extension}
     *
     * @param userId       사용자 ID
     * @param historyIndex 학력/약력 인덱스
     * @param contentType  원본 파일의 Content-Type
     * @return String S3 객체 키
     */
    private String generateHistoryAttachmentKey(String userId, int historyIndex, String contentType) {
        String extension = getFileExtensionFromContentType(contentType);
        return "history-attachments/" + userId + "_history_" + historyIndex + extension;
    }

    /**
     * 학력/약력 첨부파일의 유효성을 검증합니다.
     * <p>
     * 검증 항목:
     * - 파일 존재 여부
     * - 파일 크기 제한 (10MB)
     * - 파일 타입 (PDF, 이미지, 문서 등)
     *
     * @param file 검증할 파일
     * @throws IllegalArgumentException 유효성 검증 실패 시
     */
    private void validateHistoryFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("첨부파일이 선택되지 않았습니다.");
        }

        // 학력/약력 첨부파일은 더 큰 크기 허용 (10MB)
        long maxHistoryFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxHistoryFileSize) {
            throw new IllegalArgumentException("첨부파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (!isValidHistoryFileType(contentType)) {
            throw new IllegalArgumentException("PDF, 이미지, 문서 형식의 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 파일의 Content-Type이 허용된 학력/약력 첨부파일 타입인지 확인합니다.
     *
     * @param contentType 확인할 Content-Type
     * @return boolean 허용된 파일 타입인 경우 true, 아니면 false
     */
    private boolean isValidHistoryFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        // 학력/약력 첨부파일 허용 타입 (더 넓은 범위)
        String[] allowedHistoryContentTypes = {
                "application/pdf",                    // PDF
                "image/jpeg", "image/jpg", "image/png", "image/gif", // 이미지
                "application/msword",                 // DOC
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "application/vnd.ms-excel",          // XLS
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
                "application/vnd.ms-powerpoint",     // PPT
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
                "text/plain"                         // 텍스트 파일
        };

        return Arrays.stream(allowedHistoryContentTypes)
                .anyMatch(allowedType -> allowedType.equalsIgnoreCase(contentType));
    }
}