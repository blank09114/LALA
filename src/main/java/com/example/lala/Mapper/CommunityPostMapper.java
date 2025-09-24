package com.example.lala.Mapper;

import com.example.lala.DTO.Request.CommunityPostRequest;
import com.example.lala.DTO.Response.CommunityView;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface CommunityPostMapper {


    // 게시글 목록 조회
    List<CommunityView> findCommunityAll(Map<String, Object> filters);

    // 게시글 총 개수
    int countCommunityAll(Map<String, Object> filters);

    // 게시글 상세 조회
    Optional<CommunityView> findPostDetail(String postId);

    // 게시글 존재 여부 확인
    boolean existsPostId(String postId);

    // 좋아요 수 증가
    int incrementLikeCount(String postId);

    // 좋아요 수 감소
    int decrementLikeCount(String postId);

    // 현재 좋아요 수 조회
    int getCurrentLikeCount(String postId);

    // 조회수 증가
    int incrementViewCount(String postId);

    // 새로 추가: 게시물 타입 상태 변경
    int updatePostType(@Param("postId") String postId, @Param("newPostType") int newPostType);

    // 게시물 타입 조회
    int getCurrentPostType(String postId);

    // 게시물 작성자 확인
    boolean isPostOwner(@Param("postId") String postId, @Param("userId") String userId);

    // 게시물 신고 수 증가
    int incrementReportCount(String postId);

    // 현재 신고 수 조회
    int getReportCount(String postId);

    int addPostUser (CommunityPostRequest postRequest);


    // 댓글 삭제 이유 업데이트
    void deletePostReason(String postId);

    /**
     * 게시물 삭제 (현재 테이블 구조 활용)
     */
    void deletePost(@Param("postId") String postId, @Param("deleteReason") String deleteReason);



}
