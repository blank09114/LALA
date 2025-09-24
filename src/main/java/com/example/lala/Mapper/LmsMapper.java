package com.example.lala.Mapper;

import com.example.lala.DTO.Lms.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LmsMapper {

    boolean selectCheckUserLecture(String userId, String lectureId);


    // 내가 최근에 들은 강의
    // 메인 페이지게 헤더에 세모버튼 누르면 가는 lms
    LastLecture myRecentLecture(String userId);

    /**
     * 🎯 특정 강의 정보 조회 (LMS용 - 새로 추가)
     */
    LastLecture getLectureByIdForLms(@Param("lectureId") String lectureId);

    //강의 취소
    void deleteUserLecture(
            @Param("userId") String userId,
            @Param("lectureId") String lectureId
    );

    // 그 강의에 목차들
    List<Chapter> lectureChapters(String lectureId);

    //그 목차 - 추가자료
    List<Addi> chapterAddi(String lectureChapterId);
    //그 목차 - 영상
    List<Video> chapterVideo(String lectureChapterId);
    //그 목차 - 문제
    List<Question> chapterQuestion(String lectureChapterId);

    // 추가자료 세부사항
    AddiDetail addiDetail(String materialId);
    // 영상 세부사항
    VideoDetail videoDetail(String materialId);
    // 문제 세부사항
    QuestionDetail questionDetail(String materialId);

    //걔네들의 댓글들
    List<Comment> materialComments(String materialId);
    //댓글 등록
    void  regQna(RegQna regQna);

    //강의 등록
    void regLecture(RegLecture regLecture);
    
    // 🎯 대체 강의 등록 방법 (순서 문제 해결용)
    void regLectureManual(RegLecture regLecture);

    //강의 등록에 관심분야도 같이 등록
    void insertLectureField(LectureField lectureField);

    // 목차 등록
    void insertLectureChapter(RegChapter regChapter);

    //강좌 등록 첫번쨰 단게 - Material 객체를 받아서 ID 설정
    void regMaterial(RegMaterial regMaterial);

    //등록한 material의 id 갖고오기
    String getMaterialId();

    //등록한 lecture의 id 갖고오기
    String getLastInsertedLectureId();

    //등록한 chapter의 id 갖고오기
    String getLastInsertedChapterId();

    //등록한 material의 id 갖고오기
    String getLastInsertedMaterialId();

    //등록한 question의 id 갖고오기
    String getLastInsertedQuestionId();

    //강좌 등록 두번째 단계 lectureMaterialId값은 위에 getMaterialId로 넣으면 댐
    void regContent(RegContent regContent);

    //강좌 등록 세번쨰 단계
    void regAddi(RegAddiMaterial regAddiMaterial);

    void regVideo(RegVideoMaterial regVideoMaterial);

    void regQuestionMat(RegQuestionMaterial regQuestionMaterial);

    void regQuestion(RegQuestion question);

    // 강의 진도율 관련 메소드들
    boolean isLectureInstructor(@Param("userId") String userId, @Param("lectureId") String lectureId);
    void updateProgressOnMaterialClick(@Param("userId") String userId, @Param("lectureId") String lectureId, @Param("materialId") String materialId);
    LectureProgressInfo getUserLectureProgress(@Param("userId") String userId, @Param("lectureId") String lectureId);
    void updateLectureStatusToRequested(@Param("lectureId") String lectureId);

    // ✅ 새로 추가된 검증 메서드
    boolean checkLectureExists(@Param("lectureId") String lectureId);

    // 🎯 진도율 확인 메서드 추가
    float checkProgressRate(@Param("userId") String userId, @Param("lectureId") String lectureId);

    // 🎯 특정 강의의 사용자 진도율 정보 조회 (정확한 진도율 포함)
    Map<String, Object> getUserLectureProgressInfo(@Param("userId") String userId, @Param("lectureId") String lectureId);

    // 🔍 관리자 기능: 승인 대기중인 강의 목록 조회
    List<Map<String, Object>> selectPendingLectures();

    // 🔍 관리자 기능: 강의 상태 업데이트 (승인/거부)
    void updateLectureStatus(@Param("lectureId") String lectureId, @Param("status") String status);

    // 퀴즈
    void subQuestion(SubQuestion subQuestion);

    SubmittedAnswer selectSubmittedAnswer(
            @Param("materialId") String materialId,
            @Param("userId") String userId
    );

    // 🎯 새로 추가된 검증 메서드들
    /**
     * interested_id가 데이터베이스에 존재하는지 확인
     * @param interestedId 확인할 관심분야 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean checkInterestedIdExists(@Param("interestedId") String interestedId);

    /**
     * 강의와 관심분야 연결이 이미 존재하는지 확인
     * @param lectureField 확인할 연결 정보
     * @return 이미 존재하면 true, 없으면 false
     */
    boolean checkLectureFieldExists(LectureField lectureField);

    /**
     * lecture_field 중복 등록 방지를 위한 안전한 삽입
     * @param lectureField 강의-관심분야 연결 정보
     * @return 삽입된 행의 수
     */
    int insertLectureFieldSafe(LectureField lectureField);
    
    // 🎯 강의 수정 관련 메서드들
    
    /**
     * 강의 정보 조회 (수정용)
     * @param lectureId 강의 ID
     * @return 강의 정보
     */
    Map<String, Object> getLectureInfoForEdit(@Param("lectureId") String lectureId);
    
    /**
     * 강의의 관심분야 ID 목록 조회
     * @param lectureId 강의 ID
     * @return 관심분야 ID 목록
     */
    List<String> getLectureInterestedIds(@Param("lectureId") String lectureId);
    
    /**
     * 강의 정보 수정
     * @param updateRequest 수정 요청 데이터
     */
    void updateLectureInfo(LectureUpdateRequest updateRequest);
    
    /**
     * 목차 수정
     * @param updateRequest 수정 요청 데이터
     */
    void updateChapter(RegChapter updateRequest);
    
    /**
     * 목차에 연결된 자료들 삭제
     * @param chapterId 목차 ID
     */
    void deleteChapterMaterials(@Param("chapterId") String chapterId);
    
    /**
     * 목차 삭제
     * @param chapterId 목차 ID
     */
    void deleteChapter(@Param("chapterId") String chapterId);
    
    /**
     * 목차 소유권 확인
     * @param chapterId 목차 ID
     * @param userId 사용자 ID
     * @return 소유니면 true
     */
    boolean checkChapterOwnership(@Param("chapterId") String chapterId, @Param("userId") String userId);
}
