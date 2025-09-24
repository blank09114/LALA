package com.example.lala.Mapper;

import com.example.lala.DTO.Response.AdminCommunityList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminCommunityMapper {

//    List<AdminCommunityList> findUserCommunityFilters(
//            String userId,
//            @Param("postTypes") List<String> postTypes,
//            @Param("userTypes") List<String> userTypes,
//            @Param("reports") List<String> reports,
//            @Param("nickname") String nickname,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate
//    );

    List<AdminCommunityList> findUserCommunityFilters(Map<String, Object> filters);

    int countCommunitysByFilters(Map<String, Object> filters);

}
