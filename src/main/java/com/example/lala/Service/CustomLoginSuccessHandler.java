package com.example.lala.Service;

import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 🔍 디버깅: 현재 사용자 권한 확인
        log.info("로그인 성공 - 사용자: {}", authentication.getName());
        log.info("사용자 권한: {}", authentication.getAuthorities());

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsImpl userDetails) {
            request.getSession().setAttribute("user_id", userDetails.getUserId());
            log.info("UUID 세션 저장: {}", userDetails.getUserId());
        }

        // 이메일 기억하기 기능 처리 (리다이렉트 전에 먼저 처리)
        handleRememberEmail(request, response, authentication);

        // 관리자 권한 체크
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("관리자 여부: {}", isAdmin);

        // 권한에 따른 리다이렉트 (한 번만 실행)
        if (isAdmin) {
            log.info("관리자 페이지로 리다이렉트");
            response.sendRedirect("/admin/home");
        } else {
            log.info("일반 사용자 메인 페이지로 리다이렉트");
            response.sendRedirect("/course/main");
        }
    }

    /**
     * 이메일 기억하기 기능 처리
     */
    private void handleRememberEmail(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Authentication authentication) {
        // 인증된 사용자 정보에서 이메일 주소 추출
        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetailsImpl userDetails) {
            email = userDetails.getUsername(); // 시스템에서는 username이 이메일로 사용됨
        }

        // 로그인 폼에서 전송된 이메일 기억하기 체크박스 값 확인
        String rememberEmail = request.getParameter("rememberEmail");
        log.info("rememberEmail 체크 여부: {}", rememberEmail);

        if ("on".equals(rememberEmail) && email != null) {
            // 이메일 기억하기가 체크되고 유효한 이메일이 있는 경우
            Cookie cookie = new Cookie("rememberEmail", email);
            cookie.setMaxAge(60 * 60 * 24 * 7); // 7일간 유효
            cookie.setPath("/");
            response.addCookie(cookie);
            log.info("이메일 기억하기 쿠키 설정: {}", email);
        } else {
            // 이메일 기억하기가 체크되지 않은 경우 기존 쿠키 삭제
            Cookie cookie = new Cookie("rememberEmail", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            log.info("이메일 기억하기 쿠키 삭제");
        }
    }
}