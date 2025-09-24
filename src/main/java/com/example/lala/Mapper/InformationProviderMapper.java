package com.example.lala.Mapper;

import com.example.lala.DTO.Course.IPInfo;
import com.example.lala.DTO.Course.IpInfoSummary;
import com.example.lala.DTO.Course.LectureSummary;
import com.example.lala.DTO.Response.InformationProvider;
import com.example.lala.DTO.Response.LikeProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface InformationProviderMapper {

    // 지식 제공자 상세에 들어가는 지식제공자 정보
    IPInfo IPsInfo(String userId);

    // 강의 리스트 중간에 나오는 간단한 지식제공자 정보
    List<IpInfoSummary> ipInfoSummaries();

    // 강사 소개 강의들
    List<LectureSummary> ipLectures(String userId);

    // 기존 메소드 유지
    List<LikeProvider> selectUserLikes(String userId);

    // 페이지네이션을 지원하는 새로운 메소드
    List<LikeProvider> selectUserLikesWithPagination(Map<String, Object> params);

    InformationProvider selectInformationProvider(String userId);

    int updateExternalLink(String userId, String link);

    int updateIntroduction(String userId, String introduction);

    int insertProReq(Map<String, Object> params);

    int insertProReqHistory(Map<String, Object> params);

    List<Map<String, Object>> selectProReqHistory(String userId);

    //찜 관련...못하겠어요
    LikeProvider selectOneUserLike(@Param("userId") String userId, @Param("providerId") String providerId);

    void insertUserLike(@Param("userId") String userId, @Param("providerId") String providerId);

    void deleteUserLike(@Param("userId") String userId, @Param("providerId") String providerId);



    //지식제공자 찜 기능qtr

    // 사용자가 해당 게시물에 좋아요를 눌렀는지 확인
    boolean checkUserLiked(@org.springframework.data.repository.query.Param("postId") String postId, @org.springframework.data.repository.query.Param("userId") String userId);

    // 좋아요 추가
    int addLike(@org.springframework.data.repository.query.Param("postId") String postId, @org.springframework.data.repository.query.Param("userId") String userId);

    // 좋아요 제거
    int removeLike(@org.springframework.data.repository.query.Param("postId") String postId, @org.springframework.data.repository.query.Param("userId") String userId);


}