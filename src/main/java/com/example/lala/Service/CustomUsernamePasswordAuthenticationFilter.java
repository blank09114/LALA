package com.example.lala.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 사용자 정의 이메일/패스워드 인증 필터
 * Spring Security의 기본 UsernamePasswordAuthenticationFilter를 확장하여
 * 로그인 시 추가적인 사용자 선택사항(이메일 기억하기 등)을 처리합니다.
 */
@Slf4j
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * 사용자 정의 인증 필터 생성자
     * 
     * @param authenticationManager Spring Security의 인증 관리자
     */
    public CustomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    /**
     * 인증 시도를 처리하는 메서드를 오버라이드
     * 기본 인증 로직 실행 전에 추가 파라미터를 처리합니다.
     * 
     * @param request HTTP 요청 객체 (로그인 폼 데이터 포함)
     * @param response HTTP 응답 객체
     * @return 인증 결과를 담은 Authentication 객체
     * @throws AuthenticationException 인증 실패 시 발생하는 예외
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 로그인 폼에서 전송된 "이메일 기억하기" 옵션 값 추출
        String rememberEmail = request.getParameter("rememberEmail");
        log.info("로그인 시도 - 이메일 기억하기 옵션: {}", rememberEmail);

        // 성공 핸들러에서 사용할 수 있도록 request 속성에 저장
        request.setAttribute("rememberEmail", rememberEmail);

        // 부모 클래스의 기본 인증 로직 실행
        return super.attemptAuthentication(request, response);
    }
}