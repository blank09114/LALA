package com.example.lala.API;

import com.example.lala.DTO.Response.AdminProviderRequest;
import com.example.lala.DTO.Response.AdminUserActivity;
import com.example.lala.DTO.UserDTO;
import com.example.lala.Service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminAPI {

    private final AdminUserService adminUserService;
    private final AdminFindUserService adminFindUserService;
    private final AdminPostService adminPostService;
    private final AdminFindCommunityService adminFindCommunityService;
    private final AdminFindLectureService adminFindLectureService;
    private final AdminTodayStatisService adminTodayStatisService;
    private final AdminProviderService adminProviderService;


    @GetMapping("/userlist")
    public Map<String, Object> showUserList(
            @RequestParam(required = false) List<String> userTypes,
            @RequestParam(required = false) List<String> activeTypes,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return adminFindUserService.getUsersWithPaging(userTypes, activeTypes, nickname, startDate, endDate, page, size);
    }

    @GetMapping("/communitylist")
    public Map<String, Object> showCommunityList(
                                    @RequestParam(required = false) List<String> postTypes,
                                    @RequestParam(required = false) List<String> userTypes,
                                    @RequestParam(required = false) List<String> reports,
                                    @RequestParam(required = false) String nickname,
                                    @RequestParam(required = false) LocalDate startDate,
                                    @RequestParam(required = false) LocalDate endDate,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size
                                   ){


        return adminFindCommunityService.findUserCommunityFilters(postTypes, userTypes, reports, nickname, startDate, endDate, page, size);
    }

    @GetMapping("/lecturelist")
    public Map<String, Object> showLectureList(@RequestParam(required = false) List<String> requestType,
                                               @RequestParam(required = false) String nickname,
                                               @RequestParam(required = false) LocalDate startDate,
                                               @RequestParam(required = false) LocalDate endDate,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size){

        return adminFindLectureService.findUserLectureFilters(requestType, nickname, startDate, endDate, page, size);
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> userDetail(
            @PathVariable String userId,
            @RequestParam(defaultValue = "mine") String filter,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        UserDTO user = adminUserService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Integer userType = user.getAccountTypeId();

        Map<String, List<AdminUserActivity>> grouped = adminPostService
                .getGroupedUserActivities(userId, filter, title, startDate, endDate, userType);

        Map<String, Object> result = Map.of(
                "user", user,
                "posts", grouped.getOrDefault("post", List.of()),
                "comments", grouped.getOrDefault("comment", List.of()),
                "replies", grouped.getOrDefault("reply", List.of()),
                "reviews", grouped.getOrDefault("review", List.of()),
                "lectureRequests", grouped.getOrDefault("lectureRequests", List.of())
        );

        return result;
    }

    /**
     * 지식제공자 요청 정보 조회
     */
    @GetMapping("/user/{userId}/provider-request")
    public ResponseEntity<Map<String, Object>> getProviderRequest(@PathVariable String userId) {
        try {
            // 사용자 기본 정보 조회
            UserDTO user = adminUserService.findUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 지식제공자 요청 정보 조회
            Optional<AdminProviderRequest> providerRequest = adminProviderService.findProviderRequest(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("hasRequest", providerRequest.isPresent());

            if (providerRequest.isPresent()) {
                response.put("providerRequest", providerRequest.get());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("지식제공자 요청 정보 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "지식제공자 요청 정보를 불러올 수 없습니다."));
        }
    }



    @PostMapping("/user/{userId}/userTypeNo")
    public ResponseEntity<String> updateUserType(
            @PathVariable String userId,
            @RequestBody Map<String, Integer> requestBody) {

        int userTypeNo = requestBody.get("userTypeNo");
        adminUserService.updateUserType(userId, userTypeNo);

        return ResponseEntity.ok("회원 유형 변경 완료");
    }



    @PostMapping("/user/{userId}/activity")
    public ResponseEntity<String> updateActivityType(
            @PathVariable String userId,
            @RequestBody Map<String, Integer> requestBody
    ) {
        Integer activity = requestBody.get("activity");
        adminUserService.updateActiveType(userId, activity);
        return ResponseEntity.ok("활동 타입 변경 완료");
    }

}
