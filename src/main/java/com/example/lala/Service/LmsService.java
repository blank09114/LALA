package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Lms.*;
import com.example.lala.DTO.Response.UserProfile;
import com.example.lala.Mapper.LectureMapper;
import com.example.lala.Mapper.LmsMapper;
import com.example.lala.Mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class LmsService extends BaseService {

    private final LectureMapper lectureMapper;
    private final LmsMapper lmsMapper;
    private final UserMapper userMapper;

    public boolean checkProvider(String lectureId) {
        Map<String, Object> params = Map.of("lectureId", lectureId, "userId", getCurrentUserId());
        return lectureMapper.selectProviderLecture(params);
    }

    public boolean checkUserLecture(String lectureId) {
        return lmsMapper.selectCheckUserLecture(getCurrentUserId(), lectureId);
    }

    /**
     * 🎯 사용자의 강의 접근 권한 확인 (새로 추가)
     */
    public boolean hasUserAccessToLecture(String userId, String lectureId) {
        try {
            // 1. 수강 등록 여부 확인
            boolean isEnrolled = lmsMapper.selectCheckUserLecture(userId, lectureId);
            
            // 2. 강의 소유자인지 확인 (지식 제공자)
            Map<String, Object> params = Map.of("lectureId", lectureId, "userId", userId);
            boolean isOwner = lectureMapper.selectProviderLecture(params);
            
            log.debug("사용자 {} 강의 {} 접근 권한 - 수강등록: {}, 소유자: {}", 
                     userId, lectureId, isEnrolled, isOwner);
            
            return isEnrolled || isOwner;
            
        } catch (Exception e) {
            log.error("강의 접근 권한 확인 실패 - userId: {}, lectureId: {}", userId, lectureId, e);
            return false;
        }
    }

    /**
     * 🎯 특정 강의 정보 조회 (새로 추가)
     */
    public Map<String, Object> getLectureInfoById(String lectureId) {
        try {
            log.info("[LmsService] 특정 강의 정보 조회: lectureId={}", lectureId);
            
            // 강의 기본 정보 조회 - myRecentLecture 유사한 구조로 조회
            LastLecture lectureInfo = lmsMapper.getLectureByIdForLms(lectureId);
            
            if (lectureInfo == null) {
                log.warn("[LmsService] 강의를 찾을 수 없음: lectureId={}", lectureId);
                return null;
            }
            
            // Map으로 변환하여 반환
            Map<String, Object> result = new HashMap<>();
            result.put("lectureId", lectureInfo.getLectureId());
            result.put("title", lectureInfo.getTitle());
            result.put("thumbnail", lectureInfo.getThumbnail());
            result.put("nickname", lectureInfo.getNickname());
            result.put("outline", lectureInfo.getOutline());
            result.put("introduction", lectureInfo.getIntroduction());
            result.put("price", lectureInfo.getPrice());
            result.put("discountRate", lectureInfo.getDiscountRate());
            result.put("avgReviewRate", lectureInfo.getAvgReviewRate());
            result.put("reviewNum", lectureInfo.getReviewNum());
            result.put("studentNum", lectureInfo.getStudentNum());
            result.put("difficulty", lectureInfo.getDifficulty());
            result.put("lectureStatus", lectureInfo.getLectureStatus());
            result.put("bookmarkNum", lectureInfo.getBookmarkNum());
            result.put("progressRate", lectureInfo.getProgressRate());
            result.put("big", lectureInfo.getBig());
            result.put("small", lectureInfo.getSmall());
            
            log.info("[LmsService] 특정 강의 정보 조회 성공: {}", lectureInfo.getTitle());
            return result;
            
        } catch (Exception e) {
            log.error("[LmsService] 특정 강의 정보 조회 실패: lectureId={}", lectureId, e);
            return null;
        }
    }

    /**
     * 🎯 특정 강의의 목차 조회 (새로 추가)
     */
    public List<Chapter> getLectureChaptersById(String lectureId) {
        try {
            log.info("[LmsService] 특정 강의 목차 조회: lectureId={}", lectureId);
            List<Chapter> chapters = lmsMapper.lectureChapters(lectureId);
            log.info("[LmsService] 특정 강의 목차 조회 성공: {}개 목차", chapters.size());
            return chapters;
        } catch (Exception e) {
            log.error("[LmsService] 특정 강의 목차 조회 실패: lectureId={}", lectureId, e);
            return new ArrayList<>();
        }
    }

    // 🎯 수정된 내가 최근에 본 강의 조회 메서드
    public Map<String, Object> myRecentLecture(){
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = getCurrentUserId();

            // 최근 강의 정보 조회 (진도율 포함) - last_update_date 기준으로 가장 최근
            LastLecture recentLecture = lmsMapper.myRecentLecture(userId);

            if (recentLecture != null) {
                log.info("[LmsService] 최근 강의 조회 성공: 강의ID={}, 기본진도율={}%, 강의명={}",
                        recentLecture.getLectureId(),
                        recentLecture.getProgressRate(),
                        recentLecture.getTitle());

                // 🎯 진도율 정보를 별도로 다시 조회하여 정확한 값 확보
                try {
                    Map<String, Object> progressInfo = lmsMapper.getUserLectureProgressInfo(userId, recentLecture.getLectureId());
                    if (progressInfo != null && progressInfo.get("real_time_progress_rate") != null) {
                        Float actualProgressRate = ((Number) progressInfo.get("real_time_progress_rate")).floatValue();
                        log.info("[LmsService] 실제 진도율 조회: {}% (기존: {}%)",
                                actualProgressRate, recentLecture.getProgressRate());

                        // 🎯 실제 진도율로 업데이트
                        recentLecture.setProgressRate(actualProgressRate);
                    } else {
                        log.warn("[LmsService] 진도율 정보를 찾을 수 없어 기본값 0% 사용");
                        recentLecture.setProgressRate(0.0f);
                    }
                } catch (Exception e) {
                    log.error("[LmsService] 진도율 조회 실패, 기본값 사용: {}", e.getMessage());
                    recentLecture.setProgressRate(0.0f);
                }

            } else {
                log.warn("[LmsService] 최근 강의가 없습니다. 사용자ID: {}", userId);
                result.put("lecture", null);
                result.put("userInfo", userMapper.selectById(userId));
                result.put("hasLecture", false);
                return result;
            }

            result.put("lecture", recentLecture);
            result.put("userInfo", userMapper.selectById(userId));
            result.put("hasLecture", true);

            return result;
        } catch (Exception e) {
            log.error("[LmsService] 최근 강의 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("강의 정보 조회에 실패했습니다.");
        }
    }

    /*
    수강생이 강의 취소하는 기능
    1. 만약 userInfo의 accountType의 값이 0(일반 사용자)면
    1-1. 만약 수강률이 0%이면
        강의를 취소하는 버튼이 생기고 거기에 deleteUserLecture()기능을 넣어주면 됨
    1-2. 수강률이 1% 이상이면
        수강률 그래프로 몇퍼센트인지 알려주면 됨
    */

    // 🎯 강의 취소 메서드 개선 (진도율 0%일 때만)
    public void deleteLecture(String lectureId){
        try {
            String userId = getCurrentUserId();

            // 1. 현재 진도율 확인
            Map<String, Object> progressInfo = lmsMapper.getUserLectureProgressInfo(userId, lectureId);
            if (progressInfo == null) {
                throw new RuntimeException("해당 강의를 찾을 수 없습니다.");
            }

            Float progressRate = 0.0f;
            if (progressInfo.get("real_time_progress_rate") != null) {
                progressRate = ((Number) progressInfo.get("real_time_progress_rate")).floatValue();
            }

            // 2. 진도율이 0%인지 확인
            if (progressRate > 0) {
                throw new RuntimeException("이미 수강을 시작한 강의는 취소할 수 없습니다. (현재 진도율: " + progressRate + "%)");
            }

            // 3. 강의 취소 처리
            log.info("[LmsService] 강의 취소 시작: userId={}, lectureId={}, 진도율={}%",
                    userId, lectureId, progressRate);

            lmsMapper.deleteUserLecture(userId, lectureId);

            log.info("[LmsService] 강의 취소 완료: userId={}, lectureId={}", userId, lectureId);

        } catch (Exception e) {
            log.error("[LmsService] 강의 취소 실패: userId={}, lectureId={}, error={}",
                    getCurrentUserId(), lectureId, e.getMessage());
            throw e;
        }
    }

    // 🎯 새로 추가된 진도율 확인 메서드
    public boolean canCancelLecture(String lectureId) {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> progressInfo = lmsMapper.getUserLectureProgressInfo(userId, lectureId);

            if (progressInfo == null) {
                return false;
            }

            Float progressRate = 0.0f;
            if (progressInfo.get("real_time_progress_rate") != null) {
                progressRate = ((Number) progressInfo.get("real_time_progress_rate")).floatValue();
            }

            return progressRate == 0.0f;
        } catch (Exception e) {
            log.error("[LmsService] 취소 가능 여부 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    // 최근 들은 강의의 목차들
    /*
    여기에 들어오는 lectureId는 위에 lmsMapper.myRecentLecturer에 들어있는
    lectureId값을 가져와서 사용하면 됨
    */
    /*
    원래 위에 방식이었는데 리팩토링해서 이거로 바뀜
    그리고 getmapping에 lectureId 안넣어도 됌
    */
    public List<Chapter> lectureChapters(){
        return lmsMapper.lectureChapters(
                lmsMapper.myRecentLecture(getCurrentUserId()).getLectureId()
        );
    }

    /* public List<Chapter> lectureChapters2(){
        //LMAT값을 가지고 lectureId값을 반환하는 함수
        return lmsMapper.lectureChapters(lectureId);
    } */

    // 🎯 특정 목차의 자료들만 조회하는 메서드
    public Map<String, Object> getChapterMaterials(String chapterId) {
        try {
            log.info("[LmsService] 목차별 자료 조회: chapterId={}", chapterId);

            // 특정 목차의 자료들만 조회
            List<Addi> chapterAddi = lmsMapper.chapterAddi(chapterId);
            List<Video> chapterVideo = lmsMapper.chapterVideo(chapterId);
            List<Question> chapterQuestion = lmsMapper.chapterQuestion(chapterId);

            log.info("[LmsService] 목차 {} 자료 조회 완료: 추가자료 {}개, 영상 {}개, 문제 {}개",
                    chapterId, chapterAddi.size(), chapterVideo.size(), chapterQuestion.size());

            // 결과 맵 구성하여 반환
            Map<String, Object> result = new HashMap<>();
            result.put("chapterId", chapterId);
            result.put("addi", chapterAddi);
            result.put("video", chapterVideo);
            result.put("question", chapterQuestion);

            // 🎯 목차별 자료를 통합 리스트로도 제공
            List<Object> allMaterials = new ArrayList<>();
            allMaterials.addAll(chapterAddi);
            allMaterials.addAll(chapterVideo);
            allMaterials.addAll(chapterQuestion);
            result.put("materials", allMaterials);
            result.put("totalCount", allMaterials.size());

            return result;

        } catch (Exception e) {
            log.error("[LmsService] 목차별 자료 조회 실패: chapterId={}, error={}", chapterId, e.getMessage());
            throw new RuntimeException("목차 자료 조회에 실패했습니다.");
        }
    }

    /*
    여기에 들어오는 lectureChapterId또한 마찬가지로 위에 lmsMapper.lectureChapters에 들어있는
    chapterId값을 가져와서 사용하면 됨
    */
    /*
    얘네도 리팩토링 성공함 매개변수로 chapterId 안받아도 됌
    */
    // 최근 들은 강의의 목차들의 자료들 (목차별 구분)
    public Map<String, Object> lectureMaterials(){
        String lectureId = lmsMapper.myRecentLecture(getCurrentUserId()).getLectureId();
        List<Chapter> chapters = lmsMapper.lectureChapters(lectureId);

        log.info("[LmsService] 목차별 자료 조회 시작: lectureId={}, 목차수={}", lectureId, chapters.size());

        // ✅ 목차별로 구분된 자료 맵
        Map<String, Object> result = new HashMap<>();
        Map<String, List<Object>> chapterMaterials = new HashMap<>();

        // 전체 자료 리스트 (기존 호환성 유지)
        List<Addi> allAddi = new ArrayList<>();
        List<Video> allVideo = new ArrayList<>();
        List<Question> allQuestion = new ArrayList<>();

        for(Chapter chapter : chapters){
            String chapterId = chapter.getChapterId();
            log.info("[LmsService] 목차 {} 자료 조회 중...", chapterId);

            // 각 목차별 자료들 조회
            List<Addi> chapterAddi = lmsMapper.chapterAddi(chapterId);
            List<Video> chapterVideo = lmsMapper.chapterVideo(chapterId);
            List<Question> chapterQuestion = lmsMapper.chapterQuestion(chapterId);

            log.info("[LmsService] 목차 {} 자료: 추가자료 {}개, 영상 {}개, 문제 {}개",
                    chapterId, chapterAddi.size(), chapterVideo.size(), chapterQuestion.size());

            // ✅ 목차별 자료 맵에 추가
            List<Object> materials = new ArrayList<>();
            materials.addAll(chapterAddi);
            materials.addAll(chapterVideo);
            materials.addAll(chapterQuestion);
            chapterMaterials.put(chapterId, materials);

            // 전체 리스트에도 추가 (기존 호환성)
            allAddi.addAll(chapterAddi);
            allVideo.addAll(chapterVideo);
            allQuestion.addAll(chapterQuestion);
        }

        // ✅ 결과 맵 구성
        result.put("addi", allAddi);
        result.put("video", allVideo);
        result.put("question", allQuestion);
        result.put("byChapter", chapterMaterials); // 목차별 구분된 자료

        log.info("[LmsService] 자료 조회 완료: 전체 추가자료 {}개, 영상 {}개, 문제 {}개",
                allAddi.size(), allVideo.size(), allQuestion.size());

        return result;
    }

    // 목차에서 추가자료 눌렀을 때 보여줄 자료 세부사항
    public AddiDetail addiDetail(String materialId){
        return lmsMapper.addiDetail(materialId);
    }

    // 목차에서 강의 눌렀을 때 보여줄 강의 세부사항
    public VideoDetail videoDetail(String materialId){
        return lmsMapper.videoDetail(materialId);
    }

    // 목차에서 문제 눌렀을 때 보여줄 문제 세부사항
    public QuestionDetail questionDetail(String materialId){
        return lmsMapper.questionDetail(materialId);
    }

    /*
    그럼 위에서 추가자료/영상/문제를 클릭하게 되면 걔가 갖고있는
    matId가 튀어나와서 여기에 매개변수에 들어감
    그러면 그 material의 댓글들이 나오게 돼ㅔㅁ
    */
    public List<Comment> materialComments(String materialId){
        return lmsMapper.materialComments(materialId);
    }

    // 댓글 등록 (사용자 아이디값은 service에서 처리, 입력된 값만 갖고오면됩)
    public void regQna(RegQna regQna){

        RegQna qna = RegQna.builder()
                .lectureContentId(regQna.getLectureContentId())
                .questionalId(getCurrentUserId())
                .questionContent(regQna.getQuestionContent())
                .build();

        lmsMapper.regQna(qna);
    }

    // 강의 등록 (지식 제공자인 경우)
    // 강의, 관심분야 같이 폼에 입력받음
    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public String regLecture(RegLecture regLecture, LectureField lectureField) {
        try {
            // uploaderId는 서버에서 강제로 설정
            String uploaderId = getCurrentUserId();
            log.info("[LmsService] 강의 등록 시작: uploaderId={}, 강의명={}", uploaderId, regLecture.getName());

            // 1. 🎯 입력값 검증 - interestedId가 실제로 존재하는지 확인
            String interestedId = lectureField.getInterestedId();
            if (interestedId == null || interestedId.trim().isEmpty()) {
                throw new IllegalArgumentException("관심분야(interestedId)는 필수입니다.");
            }
            
            // 🎯 interested 테이블에 해당 ID가 존재하는지 확인
            boolean isValidInterestedId = lmsMapper.checkInterestedIdExists(interestedId);
            if (!isValidInterestedId) {
                log.error("[LmsService] 존재하지 않는 interestedId: {}", interestedId);
                throw new IllegalArgumentException("유효하지 않은 관심분야 ID입니다: " + interestedId);
            }
            log.info("[LmsService] interestedId 검증 성공: {}", interestedId);

            // 2. RegLecture 객체 생성 (나머지 값은 매개변수로 받은 변수에서 가져와 builder로 새 객체 생성)
            RegLecture lectureWithUploader = RegLecture.builder()
                    .uploaderId(uploaderId)
                    .thumbnail(regLecture.getThumbnail())
                    .name(regLecture.getName())
                    .outline(regLecture.getOutline())
                    .introduction(regLecture.getIntroduction())
                    .price(regLecture.getPrice())
                    .discountRate(regLecture.getDiscountRate())
                    .difficulty(regLecture.getDifficulty())
                    .build();

            log.info("[LmsService] 강의 기본 정보 등록 실행...");

            // 3. 🎯 강의 등록 먼저 실행
            lmsMapper.regLecture(lectureWithUploader);

            // 4. 🎯 생성된 lecture_id 확인 및 검증
            String lectureId = lectureWithUploader.getLectureId();
            
            // lecture_id가 제대로 생성되지 않은 경우 대체 방법 사용
            if (lectureId == null || lectureId.trim().isEmpty()) {
                // 🎯 대체 방법: 최근 삽입된 강의 ID 조회
                lectureId = lmsMapper.getLastInsertedLectureId();
                log.info("[LmsService] selectKey로 생성되지 않아 수동 조회: lectureId={}", lectureId);
            }
            
            // 여전히 ID가 없으면 오류
            if (lectureId == null || lectureId.trim().isEmpty()) {
                throw new RuntimeException("강의 ID 생성 실패 - lecture 테이블의 트리거나 함수를 확인하세요");
            }

            log.info("[LmsService] 강의 등록 완료, 생성된 lectureId: {}", lectureId);

            // 🎯 5. 중요: 강제로 트랜잭션 플러시를 위한 짧은 대기
            try {
                Thread.sleep(10); // 10ms 대기로 커밋 보장
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 6. 🎯 생성된 lecture_id가 실제로 DB에 존재하는지 재확인
            boolean lectureExists = lmsMapper.checkLectureExists(lectureId);
            log.info("[LmsService] 강의 존재 확인: lectureId={}, exists={}", lectureId, lectureExists);
            if (!lectureExists) {
                // 🎯 재시도: 다시 한 번 체크
                Thread.sleep(50); // 50ms 추가 대기
                lectureExists = lmsMapper.checkLectureExists(lectureId);
                log.info("[LmsService] 재시도 후 강의 존재 확인: lectureId={}, exists={}", lectureId, lectureExists);
                
                if (!lectureExists) {
                    throw new RuntimeException("강의 등록 후 검증 실패 - lectureId: " + lectureId);
                }
            }

            // 6. 🎯 LectureField 객체에 생성된 lectureId 설정
            lectureField.setLectureId(lectureId);
            log.info("[LmsService] 관심분야 등록 시작: lectureId={}, interestedId={}", 
                    lectureId, lectureField.getInterestedId());

            // 7. 🎯 관심분야 등록 실행 (외래키 제약조건 확인 후)
            // 임시로 기존 방식 사용하여 더 상세한 오류 정보 확인
            try {
                insertLectureField(lectureField);
                log.info("[LmsService] 관심분야 등록 완료 (기존 방식)");
            } catch (Exception e) {
                log.error("[LmsService] 기존 방식으로도 실패: {}", e.getMessage());
                log.error("[LmsService] 상세 오류 정보:", e);
                
                // 🎯 데이터베이스 상태 재확인
                boolean lectureStillExists = lmsMapper.checkLectureExists(lectureId);
                boolean interestedStillExists = lmsMapper.checkInterestedIdExists(lectureField.getInterestedId());
                log.error("[LmsService] 재확인 - lectureId: {} (exists: {}), interestedId: {} (exists: {})",
                         lectureId, lectureStillExists, lectureField.getInterestedId(), interestedStillExists);
                         
                throw e;
            }

            log.info("[LmsService] 강의 등록 전체 프로세스 완료: lectureId={}", lectureId);
            return lectureId;

        } catch (Exception e) {
            log.error("[LmsService] 강의 등록 실패: {}", e.getMessage(), e);
            throw new RuntimeException("강의 등록 실패: " + e.getMessage(), e);
        }
    }

    // 강의 관심분야 등록 (강의 등록할 때 강의분류 등록하는거)
    @PreAuthorize("hasRole('PROVIDER')")
    public void insertLectureField(LectureField lectureField){
        lmsMapper.insertLectureField(lectureField);
    }

    // 🎯 안전한 강의 관심분야 등록 (외래키 검증 포함)
    @PreAuthorize("hasRole('PROVIDER')")
    public void insertLectureFieldSafe(LectureField lectureField) {
        try {
            log.info("[LmsService] insertLectureFieldSafe 시작: lectureId={}, interestedId={}",
                    lectureField.getLectureId(), lectureField.getInterestedId());

            // 🎯 1. 개별 외래키 검증
            String lectureId = lectureField.getLectureId();
            String interestedId = lectureField.getInterestedId();
            
            // lecture_id 존재 확인
            boolean lectureExists = lmsMapper.checkLectureExists(lectureId);
            log.info("[LmsService] lecture_id 존재 확인: {} -> {}", lectureId, lectureExists);
            if (!lectureExists) {
                throw new RuntimeException("lecture_id가 존재하지 않습니다: " + lectureId);
            }
            
            // interested_id 존재 확인
            boolean interestedExists = lmsMapper.checkInterestedIdExists(interestedId);
            log.info("[LmsService] interested_id 존재 확인: {} -> {}", interestedId, interestedExists);
            if (!interestedExists) {
                throw new RuntimeException("interested_id가 존재하지 않습니다: " + interestedId);
            }

            // 🎯 2. 중복 체크
            boolean alreadyExists = lmsMapper.checkLectureFieldExists(lectureField);
            log.info("[LmsService] 중복 체크 결과: {}", alreadyExists);
            if (alreadyExists) {
                log.warn("[LmsService] 이미 존재하는 강의-관심분야 연결: lectureId={}, interestedId={}",
                        lectureField.getLectureId(), lectureField.getInterestedId());
                return; // 중복이면 스킵
            }

            // 🎯 3. 안전한 삽입 실행
            log.info("[LmsService] 안전한 삽입 실행 시작...");
            int insertedCount = lmsMapper.insertLectureFieldSafe(lectureField);
            log.info("[LmsService] 삽입 결과: {} rows affected", insertedCount);
            
            if (insertedCount == 0) {
                // 🎯 상세한 오류 진단
                log.error("[LmsService] 삽입 실패 - 상세 진단:");
                log.error("  - lectureId: {} (exists: {})", lectureId, lectureExists);
                log.error("  - interestedId: {} (exists: {})", interestedId, interestedExists);
                log.error("  - duplicate: {}", alreadyExists);
                
                throw new RuntimeException(String.format(
                    "강의-관심분야 연결 등록 실패: lectureId=%s(exists=%s), interestedId=%s(exists=%s), duplicate=%s",
                    lectureId, lectureExists, interestedId, interestedExists, alreadyExists
                ));
            }
            
            log.info("[LmsService] 강의-관심분야 연결 등록 성공: lectureId={}, interestedId={}",
                    lectureField.getLectureId(), lectureField.getInterestedId());
                    
        } catch (Exception e) {
            log.error("[LmsService] 강의-관심분야 연결 등록 실패: lectureId={}, interestedId={}, error={}",
                    lectureField.getLectureId(), lectureField.getInterestedId(), e.getMessage());
            log.error("[LmsService] 전체 예외 스택:", e);
            throw new RuntimeException("강의-관심분야 연결 등록 실패: " + e.getMessage(), e);
        }
    }


    // 강의 목차 등록
    @PreAuthorize("hasRole('PROVIDER')")
    public String insertLectureChapter(RegChapter regChapter){
        lmsMapper.insertLectureChapter(regChapter);
        String chapterId = lmsMapper.getLastInsertedChapterId();  // 트리거로 생성된 ID 조회
        log.debug("[DEBUG] 생성된 목차 ID: " + chapterId);
        return chapterId;
    }

    /*
     * 강의 자료 등록할때의 로직
     * 1. lecture_material에 데이터 삽입
     * 2. 방금 만들어진 material_id로
     * 3. lecture_content에 데이터 삽입
     * 4. lecture_question/video_material, additional_file에 데이터 삽입
     * 5. question은 별개로 삽입
     * */


    // 강의 자료 등록 (기존 방식과 새로운 방식 모두 지원)
    @PreAuthorize("hasRole('PROVIDER')")
    public String regMaterial(int type){
        RegMaterial regMaterial = RegMaterial.builder()
                .type(type)
                .build();
        lmsMapper.regMaterial(regMaterial);
        String materialId = lmsMapper.getLastInsertedMaterialId();  // 트리거로 생성된 ID 조회
        log.debug("[DEBUG] 생성된 자료 ID: " + materialId);
        return materialId;
    }

    //방금 만든 material_id 갖고오기 (호환성을 위해 유지)
    @PreAuthorize("hasRole('PROVIDER')")
    public String getMaterialId(){
        return lmsMapper.getMaterialId();
    }


    // 여기 밑으로는 getMaterialId()와 현재 작업중인 lecture_id를 갖고와서
    // 사용하면 됌

    // 강의 내용 등록
    @PreAuthorize("hasRole('PROVIDER')")
    public void regContent(RegContent regContent){
        lmsMapper.regContent(regContent);
    }

    // 추가 자료 등록
    @PreAuthorize("hasRole('PROVIDER')")
    public void regAddi(RegAddiMaterial regAddiMaterial){
        lmsMapper.regAddi(regAddiMaterial);
    }

    // 추가 영상 등록
    @PreAuthorize("hasRole('PROVIDER')")
    public void regVideo(RegVideoMaterial regVideoMaterial){
        lmsMapper.regVideo(regVideoMaterial);
    }

    // 추가 문제 등록
    @PreAuthorize("hasRole('PROVIDER')")
    public void regQuestionMat(RegQuestionMaterial regQuestionMaterial){
        lmsMapper.regQuestionMat(regQuestionMaterial);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    public void regQuestion(RegQuestion question){
        lmsMapper.regQuestion(question);
    }

    // 🚀 **전체 강의 생성 통합 API** 🚀
    /**
     * .@Transactional 전체 강의 생성 절차를 하나의 트랜잭션으로 한 번에 처리하는 메서드
     * 강의 등록 → 관심분야 등록 → 목차 등록 → 자료 등록을 순차적으로 실행
     *
     * @param request 전체 강의 생성 요청 데이터
     * @return 생성된 강의 ID와 처리 결과
     */
    // LmsService.java에 임시로 추가할 간단한 테스트 메서드

    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public Map<String, Object> createCompleteLecture(LectureCreationRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> processedChapters = new ArrayList<>();

        try {
            log.info("[LmsService] 🚀 전체 강의 생성 시작: uploaderId={}", getCurrentUserId());

            // 1. 기본 정보 검증
            String uploaderId = getCurrentUserId();
            List<String> interestedIds = request.getInterestedIds();
            RegLecture lectureInfo = request.getLectureInfo();
            List<LectureCreationRequest.ChapterWithMaterials> chapters = request.getChapters();

            log.info("[LmsService] 📋 요청 데이터 확인: 강의명={}, 카테고리수={}, 목차수={}",
                    lectureInfo != null ? lectureInfo.getName() : "null",
                    interestedIds != null ? interestedIds.size() : 0,
                    chapters != null ? chapters.size() : 0);

            // 2. 입력값 검증
            if (interestedIds == null || interestedIds.isEmpty()) {
                throw new RuntimeException("카테고리가 선택되지 않았습니다.");
            }

            if (lectureInfo == null) {
                throw new RuntimeException("강의 정보가 없습니다.");
            }

            if (lectureInfo.getName() == null || lectureInfo.getName().trim().isEmpty()) {
                throw new RuntimeException("강의명이 비어있습니다.");
            }

            // 3. 📚 강의 기본 정보 등록
            log.info("[LmsService] 📚 강의 기본 정보 등록 시작...");

            RegLecture lectureWithUploader = RegLecture.builder()
                    .uploaderId(uploaderId)
                    .name(lectureInfo.getName())
                    .outline(lectureInfo.getOutline())
                    .introduction(lectureInfo.getIntroduction())
                    .thumbnail(lectureInfo.getThumbnail())
                    .price(lectureInfo.getPrice())
                    .discountRate(lectureInfo.getDiscountRate())
                    .difficulty(lectureInfo.getDifficulty())
                    .build();

            lmsMapper.regLecture(lectureWithUploader);
            String lectureId = lectureWithUploader.getLectureId();

            if (lectureId == null || lectureId.trim().isEmpty()) {
                lectureId = lmsMapper.getLastInsertedLectureId();
            }

            if (lectureId == null || lectureId.trim().isEmpty()) {
                throw new RuntimeException("강의 ID 생성 실패");
            }

            log.info("[LmsService] ✅ 강의 등록 완료: lectureId={}", lectureId);

            // 4. 🏷️ 카테고리 등록
            log.info("[LmsService] 🏷️ 카테고리 등록 시작: {}개", interestedIds.size());

            for (int i = 0; i < interestedIds.size(); i++) {
                String interestedId = interestedIds.get(i).trim();
                
                // 🎯 각 interestedId 검증
                boolean isValidInterestedId = lmsMapper.checkInterestedIdExists(interestedId);
                if (!isValidInterestedId) {
                    throw new RuntimeException("유효하지 않은 관심분야 ID: " + interestedId);
                }
                
                log.info("[LmsService] 카테고리 {}번 등록: lectureId={}, interestedId={}",
                        i + 1, lectureId, interestedId);

                LectureField lectureField = LectureField.builder()
                        .lectureId(lectureId)
                        .interestedId(interestedId)
                        .build();

                // 🎯 안전한 삽입 사용
                insertLectureFieldSafe(lectureField);
                log.info("[LmsService] ✅ 카테고리 {}번 등록 성공", i + 1);
            }

            // 5. 📖 목차 및 자료 등록
            if (chapters != null && !chapters.isEmpty()) {
                log.info("[LmsService] 📖 목차 등록 시작: {}개", chapters.size());

                for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
                    LectureCreationRequest.ChapterWithMaterials chapterData = chapters.get(chapterIndex);
                    Map<String, Object> chapterResult = new HashMap<>();

                    try {
                        log.info("[LmsService] 📖 목차 {}번 등록: {}",
                                chapterIndex + 1, chapterData.getChapterInfo().getName());

                        // 5-1. 목차 등록
                        RegChapter regChapter = RegChapter.builder()
                                .lectureId(lectureId)
                                .name(chapterData.getChapterInfo().getName())
                                .objective(chapterData.getChapterInfo().getObjective())
                                .orderNum(chapterData.getChapterInfo().getOrderNum())
                                .build();

                        lmsMapper.insertLectureChapter(regChapter);
                        String chapterId = lmsMapper.getLastInsertedChapterId();

                        if (chapterId == null || chapterId.trim().isEmpty()) {
                            throw new RuntimeException("목차 ID 생성 실패");
                        }

                        chapterResult.put("chapterId", chapterId);
                        chapterResult.put("chapterName", chapterData.getChapterInfo().getName());
                        chapterResult.put("materials", new ArrayList<>());

                        log.info("[LmsService] ✅ 목차 {}번 등록 완료: chapterId={}", chapterIndex + 1, chapterId);

                        // 5-2. 자료 등록
                        List<LectureCreationRequest.MaterialWithContent> materials = chapterData.getMaterials();
                        if (materials != null && !materials.isEmpty()) {
                            log.info("[LmsService] 📄 자료 등록 시작: {}개", materials.size());

                            List<Map<String, Object>> materialResults = new ArrayList<>();

                            for (int matIndex = 0; matIndex < materials.size(); matIndex++) {
                                LectureCreationRequest.MaterialWithContent materialData = materials.get(matIndex);

                                try {
                                    log.info("[LmsService] 📄 자료 {}번 등록: 타입={}, 제목={}",
                                            matIndex + 1,
                                            materialData.getMaterialType(),
                                            materialData.getContentInfo() != null ? materialData.getContentInfo().getName() : "null");

                                    Map<String, Object> materialResult = processMaterial(materialData, chapterId, lectureId);
                                    materialResults.add(materialResult);

                                    log.info("[LmsService] ✅ 자료 {}번 등록 완료: materialId={}",
                                            matIndex + 1, materialResult.get("materialId"));

                                } catch (Exception e) {
                                    log.error("[LmsService] ❌ 자료 {}번 등록 실패: {}", matIndex + 1, e.getMessage());
                                    Map<String, Object> errorResult = new HashMap<>();
                                    errorResult.put("success", false);
                                    errorResult.put("error", e.getMessage());
                                    errorResult.put("materialIndex", matIndex);
                                    materialResults.add(errorResult);
                                    throw e; // 트랜잭션 롤백을 위해 예외를 다시 던짐
                                }
                            }

                            chapterResult.put("materials", materialResults);
                            chapterResult.put("materialCount", materialResults.size());
                        } else {
                            chapterResult.put("materials", new ArrayList<>());
                            chapterResult.put("materialCount", 0);
                        }

                        chapterResult.put("success", true);
                        processedChapters.add(chapterResult);

                    } catch (Exception e) {
                        log.error("[LmsService] ❌ 목차 {}번 등록 실패: {}", chapterIndex + 1, e.getMessage());
                        chapterResult.put("success", false);
                        chapterResult.put("error", e.getMessage());
                        chapterResult.put("chapterIndex", chapterIndex);
                        processedChapters.add(chapterResult);
                        throw e; // 트랜잭션 롤백을 위해 예외를 다시 던짐
                    }
                }

                log.info("[LmsService] ✅ 모든 목차 등록 완료: {}개", chapters.size());
            } else {
                log.info("[LmsService] ℹ️ 등록할 목차가 없습니다.");
            }

            // 6. 📊 최종 결과 반환
            result.put("lectureId", lectureId);
            result.put("success", true);
            result.put("message", "강의 생성 완료");
            result.put("categoriesCount", interestedIds.size());
            result.put("categories", interestedIds);
            result.put("chapters", processedChapters);
            result.put("chapterCount", processedChapters.size());

            log.info("[LmsService] 🎉 전체 강의 생성 완료: lectureId={}, 목차수={}",
                    lectureId, processedChapters.size());

        } catch (Exception e) {
            log.error("[LmsService] ❌ 강의 생성 실패: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("message", "강의 생성 실패: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            result.put("chapters", processedChapters);
            result.put("chapterCount", processedChapters.size());

            throw new RuntimeException("강의 생성 실패", e);
        }

        return result;
    }

    /**
     * 자료 등록을 처리하는 헬퍼 메서드
     * 자료 타입에 따라 적절한 등록 로직을 수행
     */
    private Map<String, Object> processMaterial(
            LectureCreationRequest.MaterialWithContent materialData,
            String chapterId,
            String lectureId) {

        Map<String, Object> materialResult = new HashMap<>();

        try {
            // 🗂1. 기본 자료 정보 등록 (타입별로 구분)
            String materialId = regMaterial(materialData.getMaterialType());
            materialResult.put("materialId", materialId);
            materialResult.put("materialType", materialData.getMaterialType());

            // 2. 자료 내용 정보 등록
            if (materialData.getContentInfo() != null) {
                materialData.getContentInfo().setLectureMaterialId(materialId);
                materialData.getContentInfo().setLectureChapterId(chapterId);
                regContent(materialData.getContentInfo());
                materialResult.put("title", materialData.getContentInfo().getName());
            }

            // 3. 자료 타입별 세부 정보 등록
            switch (materialData.getMaterialType()) {
                case 1: // 영상 자료
                    if (materialData.getVideoMaterial() != null) {
                        materialData.getVideoMaterial().setLectureMaterialId(materialId);
                        regVideo(materialData.getVideoMaterial());
                        materialResult.put("videoInfo", "영상 등록 완료");
                    }
                    break;

                case 2: // 추가 자료
                    if (materialData.getAddiMaterial() != null) {
                        materialData.getAddiMaterial().setAdditionalFileId(materialId);
                        regAddi(materialData.getAddiMaterial());
                        materialResult.put("addiInfo", "추가자료 등록 완료");
                    }
                    break;

                case 3: // 문제
                    // 1. 먼저 문제 세부 내용을 등록하여 questionId를 생성
                    if (materialData.getQuestionDetail() != null) {
                        lmsMapper.regQuestion(materialData.getQuestionDetail());
                        String questionId = lmsMapper.getLastInsertedQuestionId();  // 트리거로 생성된 ID 조회
                        log.debug("[DEBUG] 생성된 문제 ID: " + questionId);
                        materialResult.put("questionDetailInfo", "문제내용 등록 완료");

                        // 2. 생성된 questionId를 사용하여 연결 테이블에 등록
                        if (materialData.getQuestionMaterial() != null) {
                            materialData.getQuestionMaterial().setLectureMaterialId(materialId);
                            materialData.getQuestionMaterial().setQuestionId(questionId);  // 수동 조회한 ID 사용
                            regQuestionMat(materialData.getQuestionMaterial());
                            materialResult.put("questionMatInfo", "문제자료 연결 완료");
                        }
                    } else if (materialData.getQuestionMaterial() != null) {
                        // questionDetail이 없는 경우 기존 방식 유지
                        materialData.getQuestionMaterial().setLectureMaterialId(materialId);
                        regQuestionMat(materialData.getQuestionMaterial());
                        materialResult.put("questionMatInfo", "문제자료 등록 완료");
                    }
                    break;

                default:
                    materialResult.put("warning", "알 수 없는 자료 타입: " + materialData.getMaterialType());
            }

            materialResult.put("success", true);

        } catch (Exception e) {
            materialResult.put("success", false);
            materialResult.put("error", e.getMessage());
            log.error("[LmsService] 자료 등록 실패: " + e.getMessage());
            throw e; // 트랜잭션을 실행하던 도중에 예외가 발생하면 예외를 던져서 트랜잭션 중지시킴
        }

        return materialResult;
    }

    // 퀴즈 제출
    public void subQuestion(SubQuestion subQuestion) {
        lmsMapper.subQuestion(subQuestion);
    }

    // 제출 정보 불러오기
    public SubmittedAnswer getSubmittedAnswer(String materialId, String userId) {
        return lmsMapper.selectSubmittedAnswer(materialId, userId);
    }

    // 🎯 강의 수정 관련 기능 추가
    
    /**
     * 강의 정보 조회 (수정용)
     * @param lectureId 강의 ID
     * @return 강의 정보
     */
    @PreAuthorize("hasRole('PROVIDER')")
    public Map<String, Object> getLectureForEdit(String lectureId) {
        try {
            String userId = getCurrentUserId();
            
            // 강사 권한 확인
            boolean isProvider = checkProvider(lectureId);
            if (!isProvider) {
                throw new RuntimeException("해당 강의의 수정 권한이 없습니다.");
            }
            
            log.info("[LmsService] 강의 수정 정보 조회: lectureId={}, userId={}", lectureId, userId);
            
            // 강의 기본 정보 조회
            Map<String, Object> lectureInfo = lmsMapper.getLectureInfoForEdit(lectureId);
            if (lectureInfo == null) {
                throw new RuntimeException("강의를 찾을 수 없습니다.");
            }
            
            // 🎯 HTML 태그 제거 처리
            String introduction = (String) lectureInfo.get("introduction");
            if (introduction != null) {
                // HTML 태그 제거 및 정리
                String cleanIntroduction = cleanHtmlTags(introduction);
                lectureInfo.put("introduction", cleanIntroduction);
                lectureInfo.put("originalIntroduction", introduction); // 원본 데이터도 보관
            }
            
            // 강의 관심분야 조회
            List<String> interestedIds = lmsMapper.getLectureInterestedIds(lectureId);
            lectureInfo.put("interestedIds", interestedIds);
            
            // 강의 목차 조회
            List<Chapter> chapters = lmsMapper.lectureChapters(lectureId);
            lectureInfo.put("chapters", chapters);
            
            log.info("[LmsService] 강의 수정 정보 조회 성공: 목차 {}개", chapters.size());
            
            return lectureInfo;
            
        } catch (Exception e) {
            log.error("[LmsService] 강의 수정 정보 조회 실패: lectureId={}, error={}", lectureId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 강의 정보 수정
     * @param lectureId 강의 ID
     * @param updateRequest 수정 요청 데이터
     */
    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public void updateLecture(String lectureId, LectureUpdateRequest updateRequest) {
        try {
            String userId = getCurrentUserId();
            
            // 강사 권한 확인
            boolean isProvider = checkProvider(lectureId);
            if (!isProvider) {
                throw new RuntimeException("해당 강의의 수정 권한이 없습니다.");
            }
            
            log.info("[LmsService] 강의 정보 수정 시작: lectureId={}", lectureId);
            
            // 🎯 HTML 태그 제거 처리
            if (updateRequest.getIntroduction() != null) {
                String cleanIntroduction = cleanHtmlTags(updateRequest.getIntroduction());
                updateRequest.setIntroduction(cleanIntroduction);
            }
            
            // 강의 기본 정보 수정
            updateRequest.setLectureId(lectureId);
            lmsMapper.updateLectureInfo(updateRequest);
            
            log.info("[LmsService] 강의 정보 수정 완료: lectureId={}", lectureId);
            
        } catch (Exception e) {
            log.error("[LmsService] 강의 정보 수정 실패: lectureId={}, error={}", lectureId, e.getMessage());
            throw new RuntimeException("강의 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 목차 수정
     * @param chapterId 목차 ID
     * @param updateRequest 수정 요청 데이터
     */
    @PreAuthorize("hasRole('PROVIDER')")
    public void updateChapter(String chapterId, RegChapter updateRequest) {
        try {
            String userId = getCurrentUserId();
            
            // 목차 소유권 확인
            boolean hasOwnership = lmsMapper.checkChapterOwnership(chapterId, userId);
            if (!hasOwnership) {
                throw new RuntimeException("해당 목차의 수정 권한이 없습니다.");
            }
            
            updateRequest.setChapterId(chapterId);
            lmsMapper.updateChapter(updateRequest);
            
            log.info("[LmsService] 목차 수정 완료: chapterId={}", chapterId);
            
        } catch (Exception e) {
            log.error("[LmsService] 목차 수정 실패: chapterId={}, error={}", chapterId, e.getMessage());
            throw new RuntimeException("목차 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 목차 삭제
     * @param chapterId 목차 ID
     */
    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public void deleteChapter(String chapterId) {
        try {
            String userId = getCurrentUserId();
            
            // 목차 소유권 확인
            boolean hasOwnership = lmsMapper.checkChapterOwnership(chapterId, userId);
            if (!hasOwnership) {
                throw new RuntimeException("해당 목차의 삭제 권한이 없습니다.");
            }
            
            // 목차에 연결된 자료들 먼저 삭제
            lmsMapper.deleteChapterMaterials(chapterId);
            
            // 목차 삭제
            lmsMapper.deleteChapter(chapterId);
            
            log.info("[LmsService] 목차 삭제 완료: chapterId={}", chapterId);
            
        } catch (Exception e) {
            log.error("[LmsService] 목차 삭제 실패: chapterId={}, error={}", chapterId, e.getMessage());
            throw new RuntimeException("목차 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * HTML 태그 제거 유틸리티 메서드
     * @param htmlContent HTML 태그가 포함된 내용
     * @return HTML 태그가 제거된 내용
     */
    private String cleanHtmlTags(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }
        
        // 기본적인 HTML 태그 제거
        String cleaned = htmlContent
                .replaceAll("<[^>]+>", "")  // 모든 HTML 태그 제거
                .replaceAll("&nbsp;", " ")    // non-breaking space 치환
                .replaceAll("&amp;", "&")      // HTML entity 치환
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .trim();
        
        // 연속된 공백 제거
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        return cleaned;
    }
}