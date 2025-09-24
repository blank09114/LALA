package com.example.lala.Config;

import com.example.lala.Service.CustomLoginFailureHandler;
import com.example.lala.Service.CustomLoginSuccessHandler;
import com.example.lala.Service.CustomOauth2UserService;
import com.example.lala.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 보안 설정 클래스
 * 
 * 애플리케이션의 전반적인 보안 정책을 정의합니다.
 * 인증(Authentication)과 인가(Authorization) 규칙을 설정하며,
 * 일반 로그인과 OAuth2 소셜 로그인을 모두 지원합니다.
 * 
 * 주요 기능:
 * - HTTP 요청 권한 관리
 * - 폼 기반 로그인 설정
 * - OAuth2 소셜 로그인 설정
 * - 세션 관리 및 동시 로그인 제한
 * - CORS 정책 설정
 * - 비밀번호 암호화 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomLoginFailureHandler customLoginFailureHandler;
    private final CustomOauth2UserService customOauth2UserService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    /**
     * Spring Security의 보안 필터 체인을 설정합니다.
     * 
     * 설정 내용:
     * - HTTP 헤더 보안 설정 (iframe 허용)
     * - URL별 접근 권한 규칙
     * - 폼 로그인 및 로그아웃 설정
     * - 세션 관리 및 동시 로그인 제한
     * - OAuth2 소셜 로그인 설정
     * - CSRF 보호 비활성화
     * - CORS 정책 적용
     * 
     * @param http HttpSecurity 설정 객체
     * @return SecurityFilterChain 보안 필터 체인
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/auth/login",
                                "/auth/signup",
                                "/auth/home",
                                "/course/main",
                                "/auth/findpasswordform",
                                "/.well-known/**",
                                "/auth/mail",
                                "/auth/verify-code",
                                "/auth/check-email",
                                "/auth/check-userid",
                                "/auth/reset-password",
                                "/auth/verify-temporary-password",
                                "/auth/communityView",
                                "/api/community/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**",
                                "/smarteditor/**",
                                "/favicon.ico",
                                "/error")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("provider")
                        .requestMatchers("/api/lms/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler(customLoginFailureHandler)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/course/main", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .userDetailsService(customUserDetailsService)
                .csrf(csrf -> csrf.disable())
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(configurationSource()))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOauth2UserService))
                        .failureHandler(customLoginFailureHandler)
                        .defaultSuccessUrl("/course/main", true)
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .permitAll()
                );

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder Bean을 생성합니다.
     * 
     * BCrypt는 adaptive hash function으로, 시간이 지남에 따라
     * 보안 강도를 조절할 수 있어 안전한 비밀번호 저장을 보장합니다.
     * 
     * @return PasswordEncoder BCrypt 암호화 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 구성합니다.
     * 
     * 다른 도메인에서 오는 HTTP 요청을 허용하기 위한 설정으로,
     * 프론트엔드와 백엔드가 다른 포트에서 실행될 때 필요합니다.
     * 
     * 설정 내용:
     * - 모든 도메인에서의 요청 허용
     * - 모든 헤더 허용
     * - GET, POST, DELETE 메서드 허용
     * 
     * @return CorsConfigurationSource CORS 설정 소스
     */
    public CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}