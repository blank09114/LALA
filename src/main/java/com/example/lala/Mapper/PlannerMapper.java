package com.example.lala.Mapper;

import com.example.lala.DTO.Response.Planner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface PlannerMapper {
    List<Planner> selectThisWeek(String userId);
    List<Planner> selectUserPlanner(Map<String, Object> params);
    
    // 주간 플래너 관련 메서드 추가
    List<Planner> selectUserPlannerByWeek(Map<String, Object> params);
    List<Planner> selectUserPlannerByMonth(Map<String, Object> params);
    int insertUserPlanner(Map<String, Object> params);
    int updatePlannerByDateAndContent(Map<String, Object> params);
    int deletePlannerByDateAndContent(Map<String, Object> params);
    List<Planner> selectByUserIdAndWeek(@Param("userId") String userId,
                                        @Param("startDate") LocalDate startDate);
    // 체크 상태 업데이트 메서드 추가
    int updatePlannerCheckedStatus(Map<String, Object> params);
}