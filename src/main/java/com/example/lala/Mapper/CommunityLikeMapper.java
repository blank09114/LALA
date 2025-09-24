package com.example.lala.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface CommunityLikeMapper {

    // 사용자가 해당 게시물에 좋아요를 눌렀는지 확인
    boolean checkUserLiked(@Param("postId") String postId, @Param("userId") String userId);

    // 좋아요 추가
    int addLike(@Param("postId") String postId, @Param("userId") String userId);

    // 좋아요 제거
    int removeLike(@Param("postId") String postId, @Param("userId") String userId);


}
