package com.example.lala.Mapper;

import com.example.lala.DTO.Response.CommunityDetails;
import com.example.lala.DTO.Response.CommunityView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface CommunityViewMapper {

    List<CommunityView> findCommunityAll(Map<String, Object> filters);


    int countCommunityAll(Map<String, Object> filters);




    Optional<CommunityView> findPostId(String postId);

    Optional<CommunityView> findPostDetail(String postId);

    List<CommunityDetails> findCommentPostDetails( String postId);


    boolean existsPostId(String postId);
    boolean existsUserId(String userId);



}
