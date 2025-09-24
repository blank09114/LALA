package com.example.lala.Controller;

import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import com.example.lala.DTO.Oauth.CustomOAuth2UserImpl;
import com.example.lala.DTO.Request.CategorySelection;
import com.example.lala.DTO.Response.Category;
import com.example.lala.DTO.Response.CategoryDetail;
import com.example.lala.DTO.UserDTO;
import com.example.lala.Service.CategoryAllService;
import com.example.lala.Service.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class UserController {

    private final UserServiceImpl userServiceimpl;
    private final CategoryAllService categoryAllService;


    @GetMapping("/signup")
    public String signUp(Model model) {
        // 회원가입 페이지를 열 때 UserDTO 객체를 만들어서 화면(form)에 넘겨주는 역할을 함.
        model.addAttribute("user", new UserDTO());
        return "auth/signupform";
    }

    @PostMapping("/signup")
    // @ModelAttribute("user") UserDTO userDTO로 값을 받을 거라면, th:object와 th:field를 사용하는게 자동 메핑에 최적화 되어 있음.
    public String signUp(@Valid @ModelAttribute("user") UserDTO userDTO,
                         RedirectAttributes redirectAttributes) { // Spring MVC에서 리다이렉트 할 때 데이터를 잠깐 전달해주는 용도로 사용.
        // 일회성 데이터를 전달하는데 쓰이는 객체 (주로 에러/성공 메시지, 안내문구 등을 일회성으로 출력할 때 사용)

        log.info("회원가입 시도 : {}", userDTO);

        try {
            userServiceimpl.signUp(userDTO);
            redirectAttributes.addFlashAttribute("success", "성공적으로 회원가입이 완료되었습니다.");
            return "redirect:auth/login";
        } catch (Exception e) {
            log.error("회원가입 오류 : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "회원가입에 실패했습니다." + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:auth/signup";
    }


    // 로그인 폼으로 이동
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "exception", required = false) String exception,
                            HttpServletRequest request,
                            Model model) {

        String rememberedEmail = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("rememberEmail".equals(cookie.getName())) {
                    rememberedEmail = cookie.getValue();
                    break;
                }
            }
        }

        model.addAttribute("rememberedEmail", rememberedEmail);

        model.addAttribute("error", error);
        model.addAttribute("exception", exception);

        return "auth/loginform";
    }

    @PostMapping("/logout")
    public String logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2UserImpl oauthUser) {
            String provider = oauthUser.getOauthProviderName();

            // ✅ 카카오 로그아웃
            if (provider.equals("kakao")) {
                String clientId = "f92152b309cecc81cccb763f54b82e25";
                String logoutRedirectUri = "http://localhost:8080/api/users/login";
                return "redirect:https://kauth.kakao.com/oauth/logout?client_id=" + clientId
                        + "&logout_redirect_uri=" + logoutRedirectUri;
            }

            // ✅ 구글 로그아웃 (단순히 세션만 만료됨. 구글 서버에는 직접 로그아웃 불가)
            if (provider.equals("google")) {
                return "redirect:/course/main"; // 세션만 만료
            }
        }

        // ✅ 일반 로그인 또는 기본 로그아웃
        return "redirect:/course/main"; // Spring Security logout URL
    }

    //비밀번호 찾기

    @GetMapping("/findpasswordform")
    public String findByPassword() {

        return "auth/findpasswordform";
    }

    // 카테고리 조회
    @GetMapping("/categoryAll")
    public String categoryAll(Model model, Authentication authentication,
                             @RequestParam(value = "fromMyPage", defaultValue = "false") boolean fromMyPage) {

        // 1. 카테고리와 관심사를 가져옴 (예: 외국어-영어, 예술-미술 등)
        List<Category> categories = categoryAllService.categoryAll();

        // 2. 현재 로그인한 사용자의 기존 선택된 카테고리 ID들 가져오기
        List<String> selectedCategoryIds = new ArrayList<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = null;
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetailsImpl userDetails) {
                userId = userDetails.getUniqueUserId();
            } else if (principal instanceof CustomOAuth2UserImpl oauth2User) {
                userId = oauth2User.getUniqueUserId();
            }

            if (userId != null) {
                try {
                    // 사용자의 기존 선택된 카테고리 조회
                    List<CategoryDetail> userCategories = categoryAllService.userCategory(userId);
                    selectedCategoryIds = userCategories.stream()
                            .map(CategoryDetail::getInterestedCategoryId)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.error("사용자 카테고리 조회 중 오류: {}", e.getMessage());
                }
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryIds", selectedCategoryIds);
        model.addAttribute("fromMyPage", fromMyPage);
        return "auth/category";
    }

    @PostMapping("/category/save")
    @ResponseBody
    public ResponseEntity<String> saveUserCategory(@RequestBody CategorySelection dto,
                                                   Authentication authentication) {


        log.info("=== 카테고리 저장 요청 시작 ===");
        log.info("Authentication: {}", authentication);
        log.info("Principal type: {}", authentication != null ? authentication.getPrincipal().getClass().getName() : "null");
        log.info("Request DTO: {}", dto);


        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("인증되지 않은 사용자입니다.");
        }

        String userId = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetailsImpl userDetails) {
            userId = userDetails.getUniqueUserId();
        } else if (principal instanceof CustomOAuth2UserImpl oauth2User) {
            userId = oauth2User.getUniqueUserId();
        } else {
            return ResponseEntity.badRequest().body("사용자 정보를 찾을 수 없습니다.");
        }

        dto.setUserId(userId); // 유저 ID 설정

        int maxSelection = 3; // 최대 선택 갯수 제한

        if (dto.getInterestedId() == null || dto.getInterestedId().size() > maxSelection) {
            return ResponseEntity.badRequest().body("관심사는 최대 " + maxSelection + "개까지 선택할 수 있습니다.");
        }

        try {
            log.info("✅ 관심사 저장 요청: {}", dto);
            categoryAllService.insertUserCategory(dto);
            return ResponseEntity.ok("성공적으로 저장됨");
        } catch (Exception e) {
            log.error("관심사 저장 중 오류 발생: ", e);
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

