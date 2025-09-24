package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Response.CalendarSchedule;
import com.example.lala.DTO.Response.Planner;
import com.example.lala.Mapper.CalendarMapper;
import com.example.lala.Mapper.PlannerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalendarService extends BaseService {
    private final CalendarMapper calendarMapper;
    private final PlannerMapper plannerMapper;

    public List<CalendarSchedule> getMonthlyEvents(Integer year, Integer month) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("월별 이벤트 조회 요청 - userId: {}, year: {}, month: {}", currentUserId, year, month);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "year", year,
                    "month", month
            );

            List<CalendarSchedule> result = calendarMapper.selectUserCal(params);
            log.info("월별 이벤트 조회 완료 - 건수: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("월별 이벤트 조회 실패 - year: {}, month: {}", year, month, e);
            throw new RuntimeException("월별 이벤트 조회에 실패했습니다.", e);
        }
    }

    @Transactional
    public int saveDailyEvent(Integer year, Integer month, Integer day, String eventText) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("일일 이벤트 저장 요청 - userId: {}, date: {}-{}-{}, content: {}",
                    currentUserId, year, month, day, eventText);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "year", year,
                    "month", month,
                    "day", day,
                    "content", eventText
            );

            int result = calendarMapper.insertUserCal(params);
            log.info("일일 이벤트 저장 완료 - 결과: {}", result);
            return result;
        } catch (Exception e) {
            log.error("일일 이벤트 저장 실패 - date: {}-{}-{}, content: {}",
                    year, month, day, eventText, e);
            throw new RuntimeException("일일 이벤트 저장에 실패했습니다.", e);
        }
    }

    @Transactional
    public int saveWeeklyEvent(String sundayDate, String content) {
        try {
            String currentUserId = getCurrentUserId();
            
            // 일요일 날짜를 기준으로 토요일 날짜 계산 (일~토 주간)
            String saturdayDate = calculateSaturdayFromSunday(sundayDate);
            
            log.info("주간 이벤트 저장 요청 - userId: {}, 주간: {} ~ {}, content: {}",
                    currentUserId, sundayDate, saturdayDate, content);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "startDate", sundayDate,  // 일요일
                    "endDate", saturdayDate,  // 토요일
                    "content", content
            );

            int result = plannerMapper.insertUserPlanner(params);
            log.info("주간 이벤트 저장 완료 - 결과: {}", result);
            return result;
        } catch (Exception e) {
            log.error("주간 이벤트 저장 실패 - sundayDate: {}, content: {}", sundayDate, content, e);
            throw new RuntimeException("주간 이벤트 저장에 실패했습니다.", e);
        }
    }

    @Transactional
    public boolean updateEventByDateAndContent(Integer year, Integer month, Integer day, String oldContent, String newContent) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("이벤트 수정 요청 - userId: {}, date: {}-{}-{}, {} -> {}",
                    currentUserId, year, month, day, oldContent, newContent);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "year", year,
                    "month", month,
                    "day", day,
                    "oldContent", oldContent,
                    "newContent", newContent
            );

            int affectedRows = calendarMapper.updateEventByDateAndContent(params);
            log.info("이벤트 수정 완료 - 영향받은 행 수: {}", affectedRows);

            return affectedRows > 0;
        } catch (Exception e) {
            log.error("이벤트 수정 실패 - date: {}-{}-{}, {} -> {}",
                    year, month, day, oldContent, newContent, e);
            throw new RuntimeException("이벤트 수정에 실패했습니다.", e);
        }
    }

    @Transactional
    public boolean updateWeeklyEventByDateAndContent(String sundayDate, String oldContent, String newContent) {
        try {
            String currentUserId = getCurrentUserId();
            String saturdayDate = calculateSaturdayFromSunday(sundayDate);
            
            log.info("주간 이벤트 수정 요청 - userId: {}, 주간: {} ~ {}, {} -> {}",
                    currentUserId, sundayDate, saturdayDate, oldContent, newContent);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "startDate", sundayDate,
                    "endDate", saturdayDate,
                    "oldContent", oldContent,
                    "newContent", newContent
            );

            int affectedRows = plannerMapper.updatePlannerByDateAndContent(params);
            log.info("주간 이벤트 수정 완료 - 영향받은 행 수: {}", affectedRows);

            return affectedRows > 0;
        } catch (Exception e) {
            log.error("주간 이벤트 수정 실패 - sundayDate: {}, {} -> {}", sundayDate, oldContent, newContent, e);
            throw new RuntimeException("주간 이벤트 수정에 실패했습니다.", e);
        }
    }

    @Transactional
    public boolean deleteEventByDateAndContent(Integer year, Integer month, Integer day, String content) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("이벤트 삭제 요청 - userId: {}, date: {}-{}-{}, content: {}",
                    currentUserId, year, month, day, content);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "year", year,
                    "month", month,
                    "day", day,
                    "content", content
            );

            int affectedRows = calendarMapper.deleteEventByDateAndContent(params);
            log.info("이벤트 삭제 완료 - 영향받은 행 수: {}", affectedRows);

            return affectedRows > 0;
        } catch (Exception e) {
            log.error("이벤트 삭제 실패 - date: {}-{}-{}, content: {}",
                    year, month, day, content, e);
            throw new RuntimeException("이벤트 삭제에 실패했습니다.", e);
        }
    }

    @Transactional
    public boolean deleteWeeklyEventByDateAndContent(String startDate, String content) {
        try {
            String currentUserId = getCurrentUserId();
            String saturdayDate = calculateSaturdayFromSunday(startDate);
            
            log.info("주간 이벤트 삭제 요청 - userId: {}, 주간: {} ~ {}, content: {}",
                    currentUserId, startDate, saturdayDate, content);

            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "startDate", startDate,
                    "content", content
            );

            int affectedRows = plannerMapper.deletePlannerByDateAndContent(params);
            log.info("주간 이벤트 삭제 완료 - 영향받은 행 수: {}", affectedRows);

            return affectedRows > 0;
        } catch (Exception e) {
            throw new RuntimeException("주간 이벤트 삭제에 실패했습니다.", e);
        }
    }

    /**
     * 일요일 날짜로부터 토요일 날짜 계산 (주간: 일~토)
     */
    private String calculateSaturdayFromSunday(String sundayDate) {
        try {
            // sundayDate는 "YYYY-MM-DD" 형식
            String[] parts = sundayDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            // 일요일에서 6일 더해서 토요일
            java.time.LocalDate sunday = java.time.LocalDate.of(year, month, day);
            java.time.LocalDate saturday = sunday.plusDays(6);
            
            return saturday.toString(); // "YYYY-MM-DD" 형식으로 반환
        } catch (Exception e) {
            log.error("토요일 날짜 계산 실패 - sundayDate: {}", sundayDate, e);
            throw new RuntimeException("토요일 날짜 계산에 실패했습니다.", e);
        }
    }

    // 주간 이벤트 조회 메서드
    public List<Planner> getWeeklyEventsBySunday(String sundayDate) {
        try {
            String currentUserId = getCurrentUserId();
            String saturdayDate = calculateSaturdayFromSunday(sundayDate);
            
            log.info("주간 이벤트 조회 요청 - userId: {}, 주간: {} ~ {}", currentUserId, sundayDate, saturdayDate);
            
            Map<String, Object> params = Map.of(
                    "userId", currentUserId,
                    "startDate", sundayDate,
                    "endDate", saturdayDate
            );

            List<Planner> result = plannerMapper.selectUserPlannerByWeek(params);
            log.info("주간 이벤트 조회 완료 - 건수: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("주간 이벤트 조회 실패 - sundayDate: {}", sundayDate, e);
            throw new RuntimeException("주간 이벤트 조회에 실패했습니다.", e);
        }
    }

/**
 * 월별 주간 이벤트 조회 (오버로드)
 */
public List<Planner> getMonthlyWeeklyEvents(Integer year, Integer month) {
    try {
        String userId = getCurrentUserId();
        
        Map<String, Object> params = Map.of(
            "userId", userId,
            "year", year,
            "month", month
        );
        
        return plannerMapper.selectUserPlannerByMonth(params);
        
    } catch (Exception e) {
        log.error("월별 주간 이벤트 조회 실패 - year: {}, month: {}, error: {}", 
                 year, month, e.getMessage());
        throw new RuntimeException("월별 주간 이벤트 조회에 실패했습니다.");
    }
}

/**
 * 주간 플래너 체크 상태 업데이트
 */
@Transactional
public boolean updatePlannerCheckedStatus(String startDate, String content, boolean completed) {
    try {
        String currentUserId = getCurrentUserId();
        String endDate = calculateSaturdayFromSunday(startDate);
        
        log.info("플래너 체크 상태 업데이트 요청 - userId: {}, 주간: {} ~ {}, content: {}, completed: {}",
                currentUserId, startDate, endDate, content, completed);

        Map<String, Object> params = Map.of(
                "userId", currentUserId,
                "startDate", startDate,
                "endDate", endDate,
                "content", content,
                "completed", completed ? 1 : 0  // boolean을 int로 변환
        );

        int affectedRows = plannerMapper.updatePlannerCheckedStatus(params);
        log.info("플래너 체크 상태 업데이트 완료 - 영향받은 행 수: {}", affectedRows);

        return affectedRows > 0;
    } catch (Exception e) {
        log.error("플래너 체크 상태 업데이트 실패 - startDate: {}, content: {}, completed: {}", 
                 startDate, content, completed, e);
        throw new RuntimeException("플래너 체크 상태 업데이트에 실패했습니다.", e);
    }
}
}