package com.example.lala.Controller;

import com.example.lala.Service.MypageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final MypageService mypageService;

    @ModelAttribute("mypageUserData")
    public Map<String, Object> injectUserData() {
        Map<String, Object> data = mypageService.getMypageUserData();

        Object userObj = data.get("User");
        if (userObj instanceof Optional<?> userOptional) {
            data.put("User", userOptional.orElse(null));
        }

        return data;
    }

    @GetMapping("/main")
    public String mypage(Model model) {
        model.addAllAttributes(mypageService.getMypageData());
        return "users/mypage/main";
    }

    @GetMapping("/account")
    public String mypageAccount(Model model) {
        Map<String, Object> mypageData = mypageService.getMypageUserData();

        // Optional<UserProfile>을 안전하게 처리
        Object userObj = mypageData.get("User");
        if (userObj instanceof Optional<?> userOptional) {
            if (userOptional.isPresent()) {
                mypageData.put("User", userOptional.get());
            } else {
                return "redirect:/error?message=" +
                        java.net.URLEncoder.encode("사용자 정보를 찾을 수 없습니다.",
                                java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        model.addAttribute("mypageUserData", mypageData);
        log.info("계정 관리 페이지 데이터: {}", model.getAttribute("mypageUserData"));
        return "users/mypage/account";
    }

    // 공통 에러 처리 메서드
    private String executeWithErrorHandling(String operation,
                                            java.util.function.Supplier<String> action,
                                            String errorMessage) {
        try {
            log.debug("{} 요청 시작", operation);
            String result = action.get();
            log.debug("{} 요청 완료", operation);
            return result;
        } catch (Exception e) {
            log.error(errorMessage, e);
            return "redirect:/error?message=" + java.net.URLEncoder.encode("데이터 로딩 중 오류가 발생했습니다.", java.nio.charset.StandardCharsets.UTF_8);
        }
    }



    @GetMapping("/activity")
    public String mypageActivity() {
        return "users/mypage/activity";
    }

    @GetMapping("/review")
    public String mypageReport(Model model) {
        return executeWithErrorHandling(
                "리뷰 페이지",
                () -> {
                    model.addAttribute("reviews", mypageService.getMyReviews());
                    return "users/mypage/review";
                },
                "리뷰 페이지 로딩 실패"
        );
    }

    @GetMapping("/cancel")
    public String mypageCancel() {
        return "users/mypage/cancel";
    }

    @GetMapping("/cart")
    public String mypageCart() {
        return "users/mypage/cart";
    }

    @GetMapping("/learning")
    public String mypageLearning() {
        return "users/mypage/learning";
    }

    @GetMapping("/likes")
    public String mypageLikes() {
        return "users/mypage/likes";
    }

    @GetMapping("/calendar")
    public String mypageCalendar() {
        return "users/mypage/calendar";
    }

    @GetMapping("/pro_req")
    public String mypagePro_req() {
        return "users/mypage/pro_req";
    }

    @GetMapping("/pro_account")
    public String mypagePro_account() {
        return "users/mypage/pro_account";
    }

    @GetMapping("/pro_learning")
    public String mypagePro_learning() {
        return "users/mypage/pro_learning";
    }

    @PostMapping("/like-provider/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, String> body) {
        String providerId = body.get("providerId");

        boolean liked = mypageService.toggleLikeProvider(providerId);

        return ResponseEntity.ok(Map.of("liked", liked));
    }


}