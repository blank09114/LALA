package com.example.lala.Controller;

import com.example.lala.DTO.AdminTodayStatisticsDTO;
import com.example.lala.DTO.UserDTO;
import com.example.lala.Service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 관리자 전용 웹 컨트롤러
 * 
 * 시스템 관리자가 사용할 수 있는 모든 관리 기능을 제공합니다.
 * ROLE_ADMIN 권한을 가진 사용자만 접근 가능하며,
 * 사용자 관리, 게시글 관리, 강의 관리 등의 기능을 포함합니다.
 * 
 * 주요 기능:
 * - 사용자 목록 조회 및 검색
 * - 사용자 상세 정보 조회
 * - 사용자 유형 및 활동 상태 변경
 * - 강의 승인 요청 처리
 * - 게시글 관리
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminFindUserService adminFindUserService;
    private final AdminPostService adminPostService;
    private final AdminFindCommunityService adminFindCommunityService;
    private final AdminTodayStatisService adminTodayStatisService;

    /**
     * 관리자 메인 페이지를 표시합니다.
     *
     * @return 관리자 메인 페이지 뷰 이름
     */
    @GetMapping("/home")
    public String adminPage(Model model) {


        List<AdminTodayStatisticsDTO> statits = adminTodayStatisService.todayStatis();
        model.addAttribute("statits", statits);

        // 현재 날짜 추가
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String formattedDate = today.format(formatter);
        model.addAttribute("currentDate", formattedDate);


        return "admin/AdminMainPage";
    }

    /**
     * 사용자 목록을 조회하고 다양한 조건으로 필터링합니다.
     * <p>
     * 지원하는 필터링 조건:
     * - 사용자 유형 (일반회원, 지식제공자 요청, 지식제공자)
     * - 활동 상태 (활동중, 활동정지)
     * - 키워드 검색 (닉네임, 이메일 등)
     * - 여러 조건의 조합 검색
     * @return 사용자 목록 페이지 뷰 이름
     */
    @GetMapping("/userlist")
    public String showUserList() {

        return "admin/userlist";
    }

    @GetMapping("/communitylist")
    public String showCommunityList() {

        return "admin/AdminCommunityList";
    }

    @GetMapping("/lecturelist")
    public String showLectureList() {

        return "admin/AdminLectureList";
    }

    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     * <p>
     * 사용자 유형에 따라 다른 뷰를 반환합니다:
     * - 지식제공자 요청(userType = 2): 강의 승인 요청 페이지
     * - 기타: 일반 사용자 상세 정보 페이지 (게시글 목록 포함)
     *
     * @param userId 조회할 사용자의 고유 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 사용자 상세 정보 또는 강의 요청 페이지 뷰 이름
     * @throws RuntimeException 해당 ID의 사용자가 존재하지 않을 경우
     */
    @GetMapping("/user/{userId}")
    public String userDetail(@PathVariable String userId, Model model) {

        UserDTO user = adminUserService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));
        model.addAttribute("user", user);

        if (user.getAccountTypeId() == 2) {
            log.info("usertype = {}", user.getAccountTypeId());
            model.addAttribute("user", user);
            return "admin/lectureRequest";
        }

        return "admin/userDetail";
    }

}