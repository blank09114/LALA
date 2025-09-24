package com.example.lala.Mapper;

import com.example.lala.DTO.Response.CalendarSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface CalendarMapper {
    List<CalendarSchedule> selectByUserId(String userId);
    List<CalendarSchedule> selectUserCal(Map<String, Object> params);
    int insertUserCal(Map<String, Object> params);
    int updateEventByDateAndContent(Map<String, Object> params);
    int deleteEventByDateAndContent(Map<String, Object> params);
    List<CalendarSchedule> selectByUserIdAndWeek(@Param("userId") String userId, @Param("startDate") LocalDate startDate);

}