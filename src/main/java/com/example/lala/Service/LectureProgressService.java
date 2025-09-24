package com.example.lala.Service;

import com.example.lala.DTO.Lms.LectureProgressInfo;
import com.example.lala.Mapper.LmsMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LectureProgressService {

    @Autowired
    private LmsMapper lmsMapper;

    /**
     * ✅ 강의 존재 여부 확인 (새로 추가된 메서드)
     */
    public boolean isLectureExists(String lectureId) {
        try {
            boolean exists = lmsMapper.checkLectureExists(lectureId);
            System.out.println("[DEBUG] 강의 ID: " + lectureId + ", 존재 여부: " + exists); // 디버깅 로그
            return exists;
        } catch (Exception e) {
            System.err.println("강의 존재 여부 확인 실패: " + e.getMessage());
            e.printStackTrace(); // 상세 오류 로그
            return false;
        }
    }

    /**
     * ✅ 사용자 수강 권한 확인 (기존 메서드 활용)
     */
    public boolean hasUserAccess(String userId, String lectureId) {
        try {
            return lmsMapper.selectCheckUserLecture(userId, lectureId);
        } catch (Exception e) {
            System.err.println("사용자 수강 권한 확인 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 🔧 LMS 페이지 진입 시 사용자 타입과 진도율 확인 (수정됨 - 진도율 포함)
     * @param userId 사용자 ID
     * @param lectureId 강의 ID
     * @return 페이지 표시 정보
     */
    public Map<String, Object> getLMSPageInfo(String userId, String lectureId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // ✅ 강의 존재 여부는 이미 API에서 확인했으므로 생략 가능

            // 1. 사용자가 지식제공자인지 확인
            boolean isInstructor = lmsMapper.isLectureInstructor(userId, lectureId);

            if (isInstructor) {
                // 지식제공자인 경우
                result.put("success", true);
                result.put("userType", "instructor");
                result.put("showEditButton", true);
                result.put("message", "더 나은 강의를 만들어주세요!");
                result.put("buttonText", "강의 수정");
                result.put("buttonColor", "green");
                result.put("showCancelButton", false);
                // ✅ 진도율 관련 정보는 지식제공자에게 불필요
                result.put("progressRate", null);

            } else {
                // 일반 사용자인 경우 - 진도율 확인
                LectureProgressInfo progressInfo = lmsMapper.getUserLectureProgress(userId, lectureId);

                if (progressInfo != null && progressInfo.isProgressStarted()) {
                    // ✅ 진도율 1% 이상 → 진도율 바 표시
                    result.put("success", true);
                    result.put("userType", "student");
                    result.put("showProgressBar", true);
                    result.put("showCancelButton", false);
                    result.put("progressRate", progressInfo.getProgressRate()); // ✅ 진도율 추가
                    result.put("lectureName", progressInfo.getLectureName());
                    result.put("buttonText", "학습 계속하기");
                    result.put("buttonColor", "blue");

                } else {
                    // ✅ 진도율 0% → 취소 버튼 표시
                    result.put("success", true);
                    result.put("userType", "student");
                    result.put("showProgressBar", false);
                    result.put("showCancelButton", true);
                    result.put("progressRate", 0.0); // ✅ 진도율 0으로 명시
                    result.put("buttonText", "강의 취소");
                    result.put("buttonColor", "red");
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("LMS 페이지 정보 조회 오류: " + e.getMessage());

            // ✅ 에러 응답도 일관된 형식으로
            result.put("success", false);
            result.put("message", "진도율 정보 조회 중 오류가 발생했습니다");
            result.put("userType", "unknown");
            result.put("buttonText", "오류");
            result.put("buttonColor", "gray");
            result.put("showCancelButton", false);
            result.put("progressRate", null);

            return result;
        }
    }

    /**
     * 자료 클릭 시 진도율 업데이트 (일반 사용자만)
     * @param userId 사용자 ID
     * @param lectureId 강의 ID
     * @param materialId 자료 ID
     * @return 업데이트된 진도율 정보
     */
    @Transactional
    public LectureProgressInfo updateMaterialProgress(String userId, String lectureId, String materialId) {
        try {
            // 1. 지식제공자인지 확인 - 지식제공자는 진도율 업데이트 안함
            boolean isInstructor = lmsMapper.isLectureInstructor(userId, lectureId);

            if (isInstructor) {
                throw new RuntimeException("지식제공자는 진도율을 업데이트할 수 없습니다.");
            }

            // 2. 진도율 업데이트 (completed_materials 기록 + lecture_content_progress_rate 갱신)
            lmsMapper.updateProgressOnMaterialClick(userId, lectureId, materialId);

            // 3. 업데이트된 진도율 조회
            LectureProgressInfo progressInfo = lmsMapper.getUserLectureProgress(userId, lectureId);

            System.out.println("진도율 업데이트 완료: " + progressInfo.getProgressRate() + "%");

            return progressInfo;

        } catch (Exception e) {
            System.err.println("진도율 업데이트 오류: " + e.getMessage());
            throw new RuntimeException("진도율 업데이트에 실패했습니다.", e);
        }
    }

    /**
     * RNB용 진도율 조회
     * @param userId 사용자 ID
     * @param lectureId 강의 ID
     * @return 진도율 정보
     */
    public LectureProgressInfo getRNBProgress(String userId, String lectureId) {
        try {
            // 지식제공자는 진도율 표시 안함
            boolean isInstructor = lmsMapper.isLectureInstructor(userId, lectureId);

            if (isInstructor) {
                return null; // RNB에 진도율 표시 안함
            }

            return lmsMapper.getUserLectureProgress(userId, lectureId);

        } catch (Exception e) {
            System.err.println("RNB 진도율 조회 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 강의 생성 완료 시 상태 변경
     * @param lectureId 강의 ID
     */
    @Transactional
    public void completeLectureCreation(String lectureId) {
        try {
            lmsMapper.updateLectureStatusToRequested(lectureId);
            System.out.println("강의 상태를 승인 요청 중으로 변경: " + lectureId);

        } catch (Exception e) {
            System.err.println("강의 상태 변경 오류: " + e.getMessage());
            throw new RuntimeException("강의 상태 변경에 실패했습니다.", e);
        }
    }

    /**
     * 🗑강의 취소 처리 (deleteUserLecture 활용)
     * @param userId 사용자 ID
     * @param lectureId 강의 ID
     * @return 취소 성공 여부
     */
    @Transactional
    public boolean cancelUserLecture(String userId, String lectureId) {
        try {
            // 기존 메서드 활용
            lmsMapper.deleteUserLecture(userId, lectureId);

            System.out.println("강의 취소 완료: " + lectureId);
            return true;

        } catch (Exception e) {
            System.err.println("강의 취소 오류: " + e.getMessage());
            return false;
        }
    }

}