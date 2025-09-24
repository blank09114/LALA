package com.example.lala.Mapper;

import com.example.lala.DTO.Request.CommunityRequest;
import com.example.lala.DTO.Response.CommunityDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityCommentMapper {

    // 댓글 등록
    void createComment(CommunityRequest comment);

    // 대댓글 등록
    void createReComment(CommunityRequest comment);

    // 댓글 삭제 (논리적 삭제)
    void deleteComment(String commentId);

    // 특정 게시물의 댓글 목록 조회
    List<CommunityDetails> findCommentsByPostId(String postId);

    // 특정 게시물의 댓글 수 조회
    int countCommentsByPostId(String postId);

    // 댓글 작성자 확인
    boolean isCommentOwner(@Param("commentId") String commentId, @Param("userId") String userId);

    // 댓글 수정
    void updateComment(@Param("commentId") String commentId, @Param("content") String content);

    // 댓글 존재 여부 확인
    boolean existsCommentId(String commentId);

    // 댓글 신고 수 증가
    int incrementReportCommentCount (String commentId);

    // 현재 댓글 신고 수 조회
    int getReportCommentCount(String commentId);
}