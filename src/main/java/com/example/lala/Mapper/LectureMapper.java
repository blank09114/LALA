package com.example.lala.Mapper;

import com.example.lala.DTO.Course.*;
import com.example.lala.DTO.Course.Lecture;
import com.example.lala.DTO.Response.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LectureMapper {
    MyLecture selectLastUpdate(String userId);

    List<LikeLecture> selectLikeLecture(Map<String, Object> params);

    List<MyLearning> selectUserLearning(Map<String, Object> params);

    boolean selectProviderLecture(Map<String, Object> params);

    int purchaseLecture(Map<String, Object> params);


    // course쪽

    // 최근 들은 강의
    LastLecture recentLecture(String userId);

    // 전체 강의
    List<LectureSummary> allLectures();

    // ✨ 새로 추가: 필터링된 강의 조회 (더보기 방식)
    List<LectureSummary> getFilteredLectures(Map<String, Object> params);

    //사용자 관심분야 갖는 강의
    List<LectureSummary> selectMyInterestLecture(String userId);

    //선택된 카테고리를 관심분야로 갖는 강의
    List<LectureSummary> selectedInterestLecture(String categoryId);

    //강의 상세
    Lecture lectureDetail(String lectureId);

    //강의의 수업 개수가 몇개인지
    int contentNum(String lectureId);

    //강의 상세 - 목차
    List<Chapter> lectureChapter(String lectureId);

    //강의 상세 - 목차 - 추가자료
    List<Addi> chapterAddi(String lectureChapterId);

    //강의 상세 - 목차 - 영상
    List<Video> chapterVideo(String lectureChapterId);

    //강의 상세 - 목차 - 문제
    List<Question> chapterQuestion(String lectureChapterId);





    // 강의 찜하기
    void regBookmark(String lectureId, String userId);

    // 찜하기 있는지
    boolean existsBookmark(Map<String, Object> params);

    // 찜 삭제하기
    int deleteBookmark(Map<String, Object> params);

    // 강의 상세에 들어가느 리뷰 등록
    void regReview(RegReview review);

    // 리뷰 삭제하기
    void delReview(Map<String, Object> params);

    // 리뷰 수정하기
    int updateReview(UpdateReview updateReview);


    List<CancelLecture> selectCancelLectures(Map<String, Object> params);

    int selectCancelLectureCount(String userId);
    // 새로 추가

    List<CancelLecture> selectCancelLectures(String userId);

    // lms쪽
    // 내가 최근에 들은 강의 정보, 진도율
    List<com.example.lala.DTO.Lms.Lecture> lmsLectureInfo(String userId);

    List<IPLecture> selectIPLecture(Map<String, Object> params);

    /**
     * 페이지네이션과 함께 강의 제공자의 강의 목록을 조회합니다.
     */
    List<IPLecture> selectIPLectureWithPagination(Map<String, Object> params);

    /**
     * 강의 제공자의 전체 강의 수를 조회합니다.
     */
    int selectIPLectureCount(Map<String, Object> params);

    // 구매 관련 메서드들
    Map<String, Object> selectLectureInfo(String lectureId);
    
    boolean checkAlreadyPurchased(Map<String, Object> params);
    
    int insertUserLecture(Map<String, Object> params);
}