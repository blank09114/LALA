package com.example.lala.Controller;

import com.example.lala.DTO.Course.LectureSummary;
import com.example.lala.Service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    // 메인페이지
    @GetMapping("/main")
    public String courseMain(Authentication authentication, Model model) {

        // 관리자 체크
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/home";  // 관리자 → admin/home
        }


        String userId = courseService.getUserId();
        model.addAttribute("user", courseService.getUserSafely(userId));

        List<LectureSummary> allLectures = courseService.selectMyInterestLecture(userId);
        List<List<LectureSummary>> partitioned = new ArrayList<>();

        for (int i = 0; i < allLectures.size(); i += 4) {
            int end = Math.min(i + 4, allLectures.size());
            partitioned.add(allLectures.subList(i, end));
        }

        model.addAttribute("lecList", partitioned);
        return "course/main";
    }

    // 강의 리스트
    @GetMapping("/mainLectureList")
    public String mainLectureList(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("searchKeyword", keyword.trim());
        }
        return "course/mainLectureList";
    }

    // 강의 상세
    @GetMapping("/detail/{lectureId}")
    public String lectureDetail(@PathVariable String lectureId, Model model) {
        String userId = courseService.getUserId();
        model.addAttribute("userId", userId);
        model.addAttribute("lectureId", lectureId); // 뷰에서는 이 값으로 fetch 요청함
        return "course/mainLectureDetail";
    }


    // 강사 상세 (강의)
    //@RequestMapping("/course")
    @GetMapping("/IPdetail/{providerId}")
    public String ipInfo(@PathVariable String providerId, Model model) {
        model.addAttribute("providerId", providerId); // 뷰에서는 이 값으로 fetch 요청함
        return "course/InstructorProfile_lecture";  //뷰 이름은 일단 그냥 만들어놨슴니다
    }

    // 강사 상세 (수강평)
    @GetMapping("/IPdetail/{providerId}/review")
    public String ipReview(@PathVariable String providerId, Model model) {
        model.addAttribute("providerId", providerId);
        return "course/InstructorProfile_review";
    }

    // lms이동버튼
    @GetMapping("/letsgoLMS")
    public String letsgoLMS(){
        return "lms/lectureViewer";
    }


}

