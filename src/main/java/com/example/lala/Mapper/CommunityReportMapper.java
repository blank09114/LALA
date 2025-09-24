package com.example.lala.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommunityReportMapper {

    // 사용자가 해당 게시물에 신고를 했는지 확인
    boolean checkUserReport (@Param("postId") String postId, @Param("userId") String userId);

    // 게시물 신고 추가
    int addPostReport(@Param("postId") String postId,
                      @Param("reportUserId") String reportUserId,
                      @Param("content") String content);

    // 댓글 신고 확인
    boolean checkUserCommentReport(@Param("commentId") String commentId, @Param("userId") String userId);

    // 댓글 신고 추가
    int addCommentReport(@Param("commentId") String commentId,
                         @Param("reportUserId") String reportUserId,
                         @Param("content") String content);

}
