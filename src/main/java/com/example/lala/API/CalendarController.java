
package com.example.lala.API;

import com.example.lala.DTO.Response.CalendarSchedule;
import com.example.lala.DTO.Response.Planner;
import com.example.lala.Service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/mypage/calendar")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CalendarController {
    
    private final CalendarService calendarService;

    @PostMapping("/events")
    public List<CalendarSchedule> getMonthlyEvents(@RequestBody Map<String, Integer> request) {
        Integer year = request.get("year");
        Integer month = request.get("month");
        
        validateYearMonth(year, month);
        
        try {
            List<CalendarSchedule> events = calendarService.getMonthlyEvents(year, month);
            log.info("월별 이벤트 조회 성공: {}-{}", year, month);
            return events;
        } catch (Exception e) {
            log.error("월별 이벤트 조회 실패", e);
            throw new RuntimeException("월별 이벤트를 조회할 수 없습니다.");
        }
    }

    @PostMapping("/weekly-events")
    public List<Planner> getMonthlyWeeklyEvents(@RequestBody Map<String, Integer> request) {
        Integer year = request.get("year");
        Integer month = request.get("month");
        
        validateYearMonth(year, month);
        
        try {
            return calendarService.getMonthlyWeeklyEvents(year, month);
        } catch (Exception e) {
            log.error("주간 이벤트 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("월별 주간 이벤트를 조회할 수 없습니다.");
        }
    }

    @PostMapping("/save-event")
    public Map<String, Object> saveEvent(@RequestBody Map<String, Object> request) {
        Integer year = (Integer) request.get("year");
        Integer month = (Integer) request.get("month");
        String eventText = (String) request.get("eventText");
        Boolean isWeekly = (Boolean) request.getOrDefault("isWeekly", false);
        
        validateEventSaveRequest(year, month, eventText);
        
        try {
            if (isWeekly) {
                return saveWeeklyEvent(request, year, month, eventText);
            } else {
                return saveDailyEvent(request, year, month, eventText);
            }
        } catch (IllegalArgumentException e) {
            log.warn("이벤트 저장 파라미터 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("이벤트 저장 실패", e);
            throw new RuntimeException("이벤트 저장에 실패했습니다.");
        }
    }

    @PostMapping("/save-weekly-event")
    public Map<String, Object> saveWeeklyEvent(@RequestBody Map<String, String> request) {
        String sundayDate = request.get("sundayDate");
        String content = request.get("content");
        
        validateWeeklyEventRequest(sundayDate, content);
        
        try {
            int result = calendarService.saveWeeklyEvent(sundayDate, content);
            log.info("주간 이벤트 저장 성공: {}, {}", sundayDate, content);
            return createSuccessResponse("주간 이벤트가 성공적으로 저장되었습니다.", 
                                       "weekly-" + result);
        } catch (Exception e) {
            log.error("주간 이벤트 저장 실패", e);
            throw new RuntimeException("주간 이벤트 저장에 실패했습니다.");
        }
    }

    @PutMapping("/update-event")
    public Map<String, String> updateEvent(@RequestBody Map<String, Object> request) {
        Integer year = (Integer) request.get("year");
        Integer month = (Integer) request.get("month");
        Integer day = (Integer) request.get("day");
        String oldContent = (String) request.get("oldContent");
        String newContent = (String) request.get("newContent");
        
        validateEventUpdateRequest(year, month, day, oldContent, newContent);
        
        try {
            boolean updated = calendarService.updateEventByDateAndContent(year, month, day, oldContent, newContent);
            if (updated) {
                log.info("이벤트 수정 성공: {}-{}-{}, {} -> {}", year, month, day, oldContent, newContent);
                return Map.of("status", "success", "message", "이벤트가 성공적으로 수정되었습니다.");
            } else {
                return Map.of("status", "error", "message", "해당 날짜와 내용의 이벤트를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("이벤트 수정 실패", e);
            throw new RuntimeException("이벤트 수정에 실패했습니다.");
        }
    }

    @PutMapping("/update-weekly-event")
    public Map<String, String> updateWeeklyEvent(@RequestBody Map<String, Object> request) {
        String startDate = (String) request.get("startDate");
        String oldContent = (String) request.get("oldContent");
        String newContent = (String) request.get("newContent");

        try {
            boolean updated = calendarService.updateWeeklyEventByDateAndContent(startDate, oldContent, newContent);
            if (updated) {
                return Map.of("status", "success", "message", "이벤트가 성공적으로 수정되었습니다.");
            } else {
                return Map.of("status", "error", "message", "해당 날짜와 내용의 이벤트를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("이벤트 수정 실패", e);
            throw new RuntimeException("이벤트 수정에 실패했습니다.");
        }
    }

    @DeleteMapping("/delete-event")
    public Map<String, String> deleteEvent(@RequestBody Map<String, Object> request) {
        Integer year = (Integer) request.get("year");
        Integer month = (Integer) request.get("month");
        Integer day = (Integer) request.get("day");
        String content = (String) request.get("content");
        
        validateEventDeleteRequest(year, month, day, content);
        
        try {
            boolean deleted = calendarService.deleteEventByDateAndContent(year, month, day, content);
            if (deleted) {
                log.info("이벤트 삭제 성공: {}-{}-{}, {}", year, month, day, content);
                return Map.of("status", "success", "message", "이벤트가 성공적으로 삭제되었습니다.");
            } else {
                return Map.of("status", "error", "message", "해당 날짜와 내용의 이벤트를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("이벤트 삭제 실패", e);
            throw new RuntimeException("이벤트 삭제에 실패했습니다.");
        }
    }

    @DeleteMapping("/delete-weekly-event")
    public Map<String, String> deleteWeeklyEvent(@RequestBody Map<String, Object> request) {
        String startDate = (String) request.get("startDate");
        String content = (String) request.get("content");

        try {
            boolean deleted = calendarService.deleteWeeklyEventByDateAndContent(startDate, content);
            if (deleted) {
                return Map.of("status", "success", "message", "이벤트가 성공적으로 삭제되었습니다.");
            } else {
                return Map.of("status", "error", "message", "해당 날짜와 내용의 이벤트를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("이벤트 삭제 실패", e);
            throw new RuntimeException("이벤트 삭제에 실패했습니다.");
        }
    }

    @PutMapping("/update-planner-check")
    public Map<String, String> updatePlannerCheck(@RequestBody Map<String, Object> request) {
        String startDate = (String) request.get("startDate");
        String content = (String) request.get("content");
        Boolean completed = (Boolean) request.get("completed");
        
        validatePlannerCheckRequest(startDate, content, completed);
        
        try {
            boolean updated = calendarService.updatePlannerCheckedStatus(startDate, content, completed);
            if (updated) {
                log.info("플래너 체크 상태 업데이트 성공: {}, {}, {}", startDate, content, completed);
                return Map.of("status", "success", "message", "체크 상태가 성공적으로 업데이트되었습니다.");
            } else {
                return Map.of("status", "error", "message", "해당 플래너를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("플래너 체크 상태 업데이트 실패", e);
            throw new RuntimeException("체크 상태 업데이트에 실패했습니다.");
        }
    }

    private Map<String, Object> saveWeeklyEvent(Map<String, Object> request, Integer year, Integer month, String eventText) {
        Integer day = (Integer) request.get("day");
        if (day == null) {
            throw new IllegalArgumentException("주간 이벤트인 경우 일자 정보가 필요합니다.");
        }
        
        String sundayDate = calculateSundayDate(year, month, day);
        int result = calendarService.saveWeeklyEvent(sundayDate, eventText);
        log.info("주간 이벤트 저장 성공: 주간 시작일 {}, {}", sundayDate, eventText);
        return createSuccessResponse("주간 이벤트가 성공적으로 저장되었습니다.", "weekly-" + result);
    }

    private Map<String, Object> saveDailyEvent(Map<String, Object> request, Integer year, Integer month, String eventText) {
        Integer day = (Integer) request.get("day");
        if (day == null) {
            throw new IllegalArgumentException("일일 이벤트인 경우 일자 정보가 필요합니다.");
        }
        
        int result = calendarService.saveDailyEvent(year, month, day, eventText);
        log.info("일일 이벤트 저장 성공: {}-{}-{}, {}", year, month, day, eventText);
        return createSuccessResponse("일일 이벤트가 성공적으로 저장되었습니다.", String.valueOf(result));
    }

    private String calculateSundayDate(Integer year, Integer month, Integer day) {
        try {
            LocalDate date = LocalDate.of(year, month, day);
            int dayOfWeek = date.getDayOfWeek().getValue();
            int daysToSubtract = dayOfWeek % 7;
            LocalDate sunday = date.minusDays(daysToSubtract);
            log.info("일요일 계산: {}-{}-{} -> {} (요일값: {})", year, month, day, sunday, dayOfWeek);
            return sunday.toString();
        } catch (Exception e) {
            log.error("일요일 날짜 계산 실패 - date: {}-{}-{}", year, month, day, e);
            throw new RuntimeException("일요일 날짜 계산에 실패했습니다.", e);
        }
    }

    private Map<String, Object> createSuccessResponse(String message, String eventId) {
        return Map.of(
            "status", "success",
            "message", message,
            "eventId", eventId
        );
    }

    private void validateYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new IllegalArgumentException("연도와 월 정보가 필요합니다.");
        }
    }

    private void validateEventSaveRequest(Integer year, Integer month, String eventText) {
        if (year == null || month == null || eventText == null || eventText.trim().isEmpty()) {
            throw new IllegalArgumentException("필수 파라미터가 누락되었습니다.");
        }
    }

    private void validateWeeklyEventRequest(String sundayDate, String content) {
        if (sundayDate == null || sundayDate.trim().isEmpty() || 
            content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("일요일 날짜와 내용이 모두 필요합니다.");
        }
    }

    private void validateEventUpdateRequest(Integer year, Integer month, Integer day, 
                                          String oldContent, String newContent) {
        if (year == null || month == null || day == null ||
            oldContent == null || oldContent.trim().isEmpty() ||
            newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("연도, 월, 일, 기존 내용, 새 내용이 모두 필요합니다.");
        }
    }

    private void validateEventDeleteRequest(Integer year, Integer month, Integer day, String content) {
        if (year == null || month == null || day == null ||
            content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("연도, 월, 일, 내용이 모두 필요합니다.");
        }
    }

    private void validatePlannerCheckRequest(String startDate, String content, Boolean completed) {
        if (startDate == null || startDate.trim().isEmpty() ||
            content == null || content.trim().isEmpty() ||
            completed == null) {
            throw new IllegalArgumentException("시작일, 내용, 완료 상태가 모두 필요합니다.");
        }
    }
}
