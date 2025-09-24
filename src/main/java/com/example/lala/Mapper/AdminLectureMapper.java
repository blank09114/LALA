package com.example.lala.Mapper;

import com.example.lala.DTO.Response.AdminLecuterList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminLectureMapper {

//    List<AdminLecuterList> findUserLectureFilters(
//            String userId,
//            @Param("requestType") List<String> requestType,
//            @Param("nickname") String nickname,
//            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
//            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
//    );

    List<AdminLecuterList> findUserLectureFilters(Map<String, Object> filters);

    // 전체 회원 수
    int countLecturesByFilters(Map<String, Object> filters);
}
