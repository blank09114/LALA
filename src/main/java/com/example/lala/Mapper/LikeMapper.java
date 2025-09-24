package com.example.lala.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

    @Delete("DELETE FROM lecture_bookmark WHERE user_id = #{userId} AND lecture_id = #{lectureId}")
    void deleteLectureLike(@Param("userId") String userId, @Param("lectureId") String lectureId);

    @Delete("DELETE FROM provider_likes WHERE user_id = #{userId} AND provider_id = #{providerId}")
    void deleteProviderLike(@Param("userId") String userId, @Param("providerId") String providerId);
}
