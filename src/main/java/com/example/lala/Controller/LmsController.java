package com.example.lala.Controller;

import com.example.lala.Config.BaseService;
import com.example.lala.Service.LectureProgressService;
import com.example.lala.Service.LmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lms")
@Slf4j
public class LmsController extends BaseService {

    @Autowired
    private LmsService lmsService;
    @Autowired
    private LectureProgressService progressService;

    /**
     * LMS 메인 페이지 - 최근 들은 강의의 목차와 진도율 표시
     * 사용자 타입에 따라 다른 UI 제공 (일반 사용자 vs 지식 제공자)
     */
    @GetMapping("/main")
    public String showLmsMainPage(Model model) {
        try {
            String userId = getCurrentUserId();
            model.addAttribute("userId", userId);
            model.addAttribute("isAdmin", false);
            model.addAttribute("lectureId", null);
            return "lms/lmsMainPage";
        } catch (RuntimeException e) {
            return "redirect:/auth/loginform";
        }
    }

    /**
     * 🎯 특정 강의 LMS 페이지 (새로 추가)
     * @param lectureId 강의 ID
     */
    @GetMapping("/main/{lectureId}")
    public String showLmsMainPageByLectureId(@PathVariable String lectureId, Model model) {
        try {
            String userId = getCurrentUserId();
            
            // 🔍 사용자가 해당 강의에 접근 권한이 있는지 확인
            boolean hasAccess = lmsService.hasUserAccessToLecture(userId, lectureId);
            
            if (!hasAccess) {
                log.warn("사용자 {}가 강의 {}에 접근 권한이 없습니다.", userId, lectureId);
                model.addAttribute("errorMessage", "해당 강의에 접근할 권한이 없습니다.");
                return "redirect:/courses"; // 강의 목록 페이지로 리다이렉트
            }
            
            log.info("사용자 {}가 강의 {} LMS에 접근", userId, lectureId);
            
            model.addAttribute("userId", userId);
            model.addAttribute("lectureId", lectureId);
            model.addAttribute("isAdmin", false);
            
            return "lms/lmsMainPage";
            
        } catch (RuntimeException e) {
            log.error("강의별 LMS 페이지 로드 실패 - lectureId: {}", lectureId, e);
            return "redirect:/auth/loginform";
        }
    }

    /**
     * 영상 자료 페이지
     * @param materialId 자료 ID
     */
    @GetMapping("/material/video/{materialId}")
    public String showVideoMaterial(@PathVariable String materialId, Model model) {
        try {
            String userId = getCurrentUserId();
            model.addAttribute("userId", userId);
            model.addAttribute("materialId", materialId);
            model.addAttribute("materialType", "video");
            return "lms/lecturePlayer";
        } catch (RuntimeException e) {
            return "redirect:/auth/loginform";
        }
    }

    /**
     * 파일 자료 페이지
     * @param materialId 자료 ID
     */
    @GetMapping("/material/file/{materialId}")
    public String showFileMaterial(@PathVariable String materialId, Model model) {
        try {
            String userId = getCurrentUserId();
            model.addAttribute("userId", userId);
            model.addAttribute("materialId", materialId);
            model.addAttribute("materialType", "file");
            return "lms/lectuerContent";
        } catch (RuntimeException e) {
            return "redirect:/auth/loginform";
        }
    }

    /**
     * 퀴즈 자료 페이지
     * @param materialId 자료 ID
     */
    @GetMapping("/material/question/{materialId}")
    public String showQuestionMaterial(@PathVariable String materialId, Model model) {
        try {
            String userId = getCurrentUserId();
            model.addAttribute("userId", userId);
            model.addAttribute("materialId", materialId);
            model.addAttribute("materialType", "question");
            return "lms/solvingQuestion";
        } catch (RuntimeException e) {
            return "redirect:/auth/loginform";
        }
    }

    // 강의 생성 페이지
    @GetMapping("/create")
    public String showLectureCreator() {
        return "lms/lectureCreator";
    }

    // 강의 수정 페이지
    @GetMapping("/edit/{lectureId}")
    public String showLectureEdit(@PathVariable String lectureId, Model model) {
        try {
            String userId = getCurrentUserId();
            log.info("[LmsController] 강의 수정 페이지 요청: lectureId={}, userId={}", lectureId, userId);
            
            // 강사 권한 확인
            boolean isProvider = lmsService.checkProvider(lectureId);
            log.info("[LmsController] 강사 권한 확인 결과: {}", isProvider);

            if (!isProvider) {
                log.warn("[LmsController] 강의 수정 권한 없음: userId={}, lectureId={}", userId, lectureId);
                return "redirect:/lms/main";
            }

            model.addAttribute("lectureId", lectureId);
            model.addAttribute("userId", userId);
            
            log.info("[LmsController] 강의 수정 페이지 접근 성공");
            return "lms/lectureEdit";
            
        } catch (Exception e) {
            log.error("[LmsController] 강의 수정 페이지 오류: {}", e.getMessage());
            return "redirect:/auth/loginform";
        }
    }


}