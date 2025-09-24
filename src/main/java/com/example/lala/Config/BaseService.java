package com.example.lala.Config;

import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import com.example.lala.DTO.Oauth.CustomOAuth2UserImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 서비스 계층의 공통 기능을 제공하는 추상 클래스
 * 현재 로그인한 사용자 정보 조회 및 로깅 기능을 포함
 */
@Slf4j
public abstract class BaseService {

    /**
     * 현재 로그인한 사용자의 ID를 반환
     * 일반 로그인과 OAuth2 로그인을 모두 지원
     *
     * @return 현재 로그인한 사용자 ID
     * @throws RuntimeException 인증되지 않은 사용자이거나 사용자 정보를 가져올 수 없는 경우
     */
    protected String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 로그인한 타입이 일반인지 소셜인지 구분 (수정)
        Object principal = authentication.getPrincipal();
        String userId = null;

        if (principal instanceof CustomUserDetailsImpl userDetails) {
            // 일반 로그인 사용자
            userId = userDetails.getUniqueUserId();
        } else if (principal instanceof CustomOAuth2UserImpl oauth2User) {
            // OAuth2 로그인 사용자
            userId = oauth2User.getUniqueUserId(); // 이 부분만 수정

        } else {
            throw new RuntimeException("사용자 인증 정보를 찾을 수 없습니다. Principal type: " + principal.getClass().getName());
        }

        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("사용자 ID가 유효하지 않습니다.");
        }

        return userId;
    }

    /**
     * 현재 로그인한 사용자 정보를 반환
     * 일반 로그인 사용자만 지원 (OAuth2 사용자는 다른 타입)
     *
     * @return 현재 로그인한 사용자 정보
     * @throws RuntimeException 인증되지 않은 사용자이거나 OAuth2 사용자인 경우
     */
    protected CustomUserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetailsImpl)) {
            throw new RuntimeException("일반 로그인 사용자만 지원합니다. (OAuth2 사용자는 getCurrentUserId() 사용)");
        }

        return (CustomUserDetailsImpl) authentication.getPrincipal();
    }

    /**
     * 작업 결과를 로깅하는 공통 메서드
     *
     * @param operation    수행한 작업명
     * @param targetUserId 대상 사용자 ID (작업을 당한 사용자)
     * @param success      작업 성공 여부
     */
    protected void logOperation(String operation, String targetUserId, boolean success) {
        try {
            String currentUserId = getCurrentUserId();

            if (success) {
                log.info("{} 성공 - 실행자: {}, 대상: {}", operation, currentUserId, targetUserId);
            } else {
                log.error("{} 실패 - 실행자: {}, 대상: {}", operation, currentUserId, targetUserId);
            }
        } catch (Exception e) {
            if (success) {
                log.info("{} 성공 - 대상: {} (실행자 정보 없음)", operation, targetUserId);
            } else {
                log.error("{} 실패 - 대상: {} (실행자 정보 없음)", operation, targetUserId);
            }
        }
    }

    /**
     * 단순 작업 결과를 로깅하는 메서드
     *
     * @param operation 수행한 작업명
     * @param success   작업 성공 여부
     */
    protected void logOperation(String operation, boolean success) {
        try {
            String currentUserId = getCurrentUserId();

            if (success) {
                log.info("{} 성공 - 사용자: {}", operation, currentUserId);
            } else {
                log.error("{} 실패 - 사용자: {}", operation, currentUserId);
            }
        } catch (Exception e) {
            if (success) {
                log.info("{} 성공 (사용자 정보 없음)", operation);
            } else {
                log.error("{} 실패 (사용자 정보 없음)", operation);
            }
        }
    }
}