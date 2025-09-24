package com.example.lala.Service;

import com.example.lala.exception.ProviderMismatchException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * 사용자 로그인 실패 시 처리를 담당하는 커스텀 핸들러 클래스
 * Spring Security의 AuthenticationFailureHandler를 구현하여 로그인 실패 시나리오를 세밀하게 처리합니다.
 * 
 * 주요 기능:
 * - 인증 예외 유형별 맞춤형 에러 메시지 제공
 * - 이메일 기억하기 기능을 위한 쿠키 관리
 * - 한글 에러 메시지의 안전한 URL 인코딩
 * - 로그인 실패 후 적절한 페이지로의 리다이렉션
 * 
 * 설정 요구사항:
 * SecurityConfig에서 failureHandler로 등록해야 예외 처리가 활성화됩니다.
 */
@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * 로그인 인증 실패 시 호출되는 메인 처리 메소드
     * 다양한 인증 예외 유형에 따라 적절한 에러 메시지를 생성하고,
     * 사용자 편의를 위한 이메일 기억하기 기능을 처리합니다.
     * 
     * 처리 과정:
     * 1. 예외 유형별 에러 메시지 생성
     * 2. 이메일 기억하기 옵션에 따른 쿠키 설정
     * 3. 에러 메시지 URL 인코딩
     * 4. 로그인 페이지로 리다이렉션
     * 
     * @param request HTTP 요청 객체 (로그인 폼 데이터 포함)
     * @param response HTTP 응답 객체 (쿠키 설정 및 리다이렉션용)
     * @param exception 발생한 인증 예외 객체
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = "";

        // 예외 유형별 사용자 친화적 에러 메시지 생성
        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "이메일이 존재하지 않습니다.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        } else if (exception instanceof ProviderMismatchException) {
            // 소셜 로그인과 일반 로그인 간 충돌 시 발생하는 커스텀 예외
            errorMessage = exception.getMessage();
        } else {
            // 예상하지 못한 기타 인증 오류
            errorMessage = "알 수 없는 오류가 발생했습니다.";
        }

        // 이메일 기억하기 기능 처리
        // 사용자가 다음 로그인 시 이메일을 자동으로 입력받을 수 있도록 쿠키 관리
        String email = request.getParameter("email");
        String rememberEmail = request.getParameter("rememberEmail");

        if ("on".equals(rememberEmail) && email != null && !email.isBlank()) {
            // 이메일 기억하기 체크 시: 7일간 유효한 쿠키 생성
            Cookie cookie = new Cookie("rememberEmail", email);
            cookie.setMaxAge(60 * 60 * 24 * 7); // 7일
            cookie.setPath("/"); // 전체 사이트에서 접근 가능
            response.addCookie(cookie);
        } else {
            // 이메일 기억하기 해제 시: 기존 쿠키 삭제
            Cookie cookie = new Cookie("rememberEmail", "");
            cookie.setMaxAge(0); // 즉시 만료
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        // 한글 에러 메시지의 URL 파라미터 전달을 위한 UTF-8 인코딩
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");

        // 로그인 실패 시 리다이렉션할 URL 설정
        // 에러 플래그와 함께 구체적인 오류 메시지를 쿼리 파라미터로 전달
        setDefaultFailureUrl("/auth/login?error=true&exception=" + errorMessage);

        // 부모 클래스의 실패 처리 로직 실행 (실제 리다이렉션 수행)
        super.onAuthenticationFailure(request, response, exception);
    }
}