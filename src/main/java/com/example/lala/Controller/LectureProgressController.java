package com.example.lala.Controller;

import com.example.lala.Config.BaseService;
import com.example.lala.Service.LectureProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 📊 LMS 수강률 페이지 Controller
 *
 * 📌 주요 기능:
 * - 수강률 상세 페이지 로딩
 * - 사용자 타입별 UI 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/lms/progress")
public class LectureProgressController extends BaseService {

    private final LectureProgressService progressService;

    /**
     * 📈 수강률 상세 페이지
     *
     * ✨ 사용자 타입에 따른 UI 분기:
     * - 지식제공자: 수정 버튼과 강의 관리 UI
     * - 일반 사용자 (진도율 0%): 취소 버튼
     * - 일반 사용자 (진도율 1% 이상): 진도율 바와 학습 현황
     *
     * @param lectureId 강의 ID
     * @param model 뷰 모델
     * @return 진도율 상세 페이지
     */
    @GetMapping("/detail/{lectureId}")
    public String progressDetailPage(@PathVariable String lectureId, Model model) {
        try {
            String userId = getCurrentUserId();

            log.info("[progressDetailPage] 수강률 상세 페이지 로딩 - 사용자: {}, 강의: {}", userId, lectureId);

            // 사용자 타입별 정보 조회
            Map<String, Object> pageInfo = progressService.getLMSPageInfo(userId, lectureId);

            if ((Boolean) pageInfo.get("success")) {
                // 📋 기본 정보 설정
                model.addAttribute("lectureId", lectureId);
                model.addAttribute("userId", userId);
                model.addAttribute("pageInfo", pageInfo);

                // 🎯 사용자 타입별 세부 정보 설정
                String userType = (String) pageInfo.get("userType");

                if ("instructor".equals(userType)) {
                    // 🎨 지식제공자 UI
                    model.addAttribute("pageTitle", "강의 관리");
                    model.addAttribute("showInstructorTools", true);

                    log.info("[progressDetailPage] 지식제공자 UI 설정");

                } else if ("student".equals(userType)) {
                    // 👤 일반 사용자 UI
                    if ((Boolean) pageInfo.getOrDefault("showProgressBar", false)) {
                        // 진도율 1% 이상 - 학습 현황 표시
                        model.addAttribute("pageTitle", "학습 현황");
                        model.addAttribute("showProgressDetails", true);

                        log.info("[progressDetailPage] 일반 사용자 (진도율 {}%) UI 설정",
                                pageInfo.get("progressRate"));

                    } else {
                        // 진도율 0% - 취소 옵션 표시
                        model.addAttribute("pageTitle", "강의 관리");
                        model.addAttribute("showCancelOption", true);

                        log.info("[progressDetailPage] 일반 사용자 (진도율 0%) UI 설정");
                    }
                }

                return "lms/progressDetail"; // Thymeleaf 템플릿

            } else {
                // ❌ 오류 발생 시
                log.error("[progressDetailPage] 페이지 정보 조회 실패: {}", pageInfo.get("message"));

                model.addAttribute("errorMessage", pageInfo.get("message"));
                return "error/lmsError";
            }

        } catch (RuntimeException e) {
            log.error("[progressDetailPage] 인증 오류: {}", e.getMessage());

            // 로그인 페이지로 리다이렉트
            return "redirect:/auth/loginform";

        } catch (Exception e) {
            log.error("[progressDetailPage] 시스템 오류: {}", e.getMessage(), e);

            model.addAttribute("errorMessage", "수강률 페이지 로딩 중 오류가 발생했습니다");
            return "error/lmsError";
        }
    }

    /**
     * 📊 수강률 대시보드 페이지 (관리자/지식제공자용)
     *
     * @param lectureId 강의 ID
     * @param model 뷰 모델
     * @return 수강률 대시보드
     */
    @GetMapping("/dashboard/{lectureId}")
    public String progressDashboard(@PathVariable String lectureId, Model model) {
        try {
            String userId = getCurrentUserId();

            log.info("[progressDashboard] 대시보드 페이지 로딩 - 사용자: {}, 강의: {}", userId, lectureId);

            // 지식제공자 권한 확인
            Map<String, Object> pageInfo = progressService.getLMSPageInfo(userId, lectureId);

            if (!"instructor".equals(pageInfo.get("userType"))) {
                log.warn("[progressDashboard] 접근 권한 없음 - 일반 사용자");
                return "redirect:/lms/lecture/" + lectureId;
            }

            // 🏗️ 대시보드 데이터 설정 (향후 확장 가능)
            model.addAttribute("lectureId", lectureId);
            model.addAttribute("pageTitle", "수강률 대시보드");
            model.addAttribute("showDashboard", true);

            log.info("[progressDashboard] 대시보드 UI 설정 완료");

            return "lms/progressDashboard";

        } catch (RuntimeException e) {
            log.error("[progressDashboard] 인증 오류: {}", e.getMessage());
            return "redirect:/auth/loginform";

        } catch (Exception e) {
            log.error("[progressDashboard] 시스템 오류: {}", e.getMessage(), e);

            model.addAttribute("errorMessage", "대시보드 로딩 중 오류가 발생했습니다");
            return "error/lmsError";
        }
    }
}