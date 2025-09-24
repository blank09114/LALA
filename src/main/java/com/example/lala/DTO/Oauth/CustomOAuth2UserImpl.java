package com.example.lala.DTO.Oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 사용자의 정보를 담는 커스텀 사용자 클래스
 * Spring Security의 OAuth2User 인터페이스를 구현하여
 * 소셜 로그인 사용자 정보를 관리합니다.
 */
public class CustomOAuth2UserImpl implements OAuth2User {

    /**
     * OAuth2 제공자로부터 받은 사용자 응답 정보
     */
    private final OAuth2UserResponse oauthUserResponse;

    /**
     * 실제 데이터베이스에 저장된 사용자 UUID
     */
    private String databaseUserId;

    /**
     * CustomOauth2User 생성자
     *
     * @param oauthUserResponse OAuth2 제공자의 사용자 응답 정보
     */
    public CustomOAuth2UserImpl(OAuth2UserResponse oauthUserResponse) {
        this.oauthUserResponse = oauthUserResponse;
    }

    /**
     * 실제 데이터베이스 사용자 ID를 설정합니다.
     *
     * @param databaseUserId 데이터베이스에 저장된 사용자 UUID
     */
    public void setDatabaseUserId(String databaseUserId) {
        this.databaseUserId = databaseUserId;
    }

    /**
     * OAuth2 사용자의 속성 정보를 반환합니다.
     * 현재는 null을 반환하도록 구현되어 있습니다.
     *
     * @return 사용자 속성 맵
     */
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    /**
     * 사용자의 권한 목록을 반환합니다.
     * 현재는 빈 컬렉션을 반환합니다.
     *
     * @return 사용자 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        return authorities;
    }

    /**
     * 사용자의 이름을 반환합니다.
     *
     * @return 사용자 이름
     */
    @Override
    public String getName() {
        return oauthUserResponse.getUserDisplayName();
    }

    /**
     * OAuth2 제공자 이름을 반환합니다.
     * Thymeleaf에서 접근 가능한 메소드입니다.
     *
     * @return 제공자 이름 (예: "kakao", "google")
     */
    public String getOauthProviderName() {
        return oauthUserResponse.getOauth2ProviderName();
    }

    /**
     * OAuth2 제공자에서의 사용자 고유 ID를 반환합니다.
     * Thymeleaf에서 접근 가능한 메소드입니다.
     *
     * @return 제공자별 사용자 고유 ID
     */
    public String getOauthProviderId() {
        return oauthUserResponse.getOauth2ProviderId();
    }

    /**
     * 사용자의 이메일 주소를 반환합니다.
     *
     * @return 사용자 이메일
     */
    public String getUserEmail() {
        return oauthUserResponse.getUserEmailAddress();
    }

    /**
     * 사용자의 프로필 이미지 URL을 반환합니다.
     *
     * @return 프로필 이미지 URL
     */
    public String getUserProfileImageUrl() {
        return oauthUserResponse.getUserProfileImageUrl();
    }

    /**
     * 실제 데이터베이스에 저장된 사용자 UUID를 반환합니다.
     * 이 메서드가 모든 사용자 식별에 사용되어야 합니다.
     *
     * @return 데이터베이스 사용자 UUID
     */
    public String getUniqueUserId() {
        return databaseUserId; // ✅ 실제 DB UUID 반환
    }

    /**
     * 사용자 닉네임을 반환합니다.
     * Thymeleaf에서 접근 가능한 메소드입니다.
     *
     * @return 사용자 닉네임
     */
    public String getUserNickname() {
        return oauthUserResponse.getUserDisplayName();
    }
}