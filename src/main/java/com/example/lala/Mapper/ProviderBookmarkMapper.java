package com.example.lala.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProviderBookmarkMapper {

    boolean checkProviderBookmark(@Param("userId") String userId, @Param("providerId") String providerId);

    void removeProviderBookmark(@Param("userId") String userId, @Param("providerId") String providerId);

    void addProviderBookmark(@Param("userId") String userId, @Param("providerId") String providerId);

}
