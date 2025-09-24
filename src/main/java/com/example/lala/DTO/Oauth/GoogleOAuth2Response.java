package com.example.lala.DTO.Oauth;

import java.util.Map;

/**
 * Google OAuth2 로그인 응답 정보를 처리하는 클래스
 * Google에서 제공하는 사용자 정보를 파싱하여 표준화된 형태로 변환합니다.
 */
public class GoogleOAuth2Response implements OAuth2UserResponse {

    /**
     * Google OAuth2 API로부터 받은 사용자 속성 정보
     */
    private final Map<String, Object> googleUserAttributes;

    /**
     * GoogleResponse 생성자
     *
     * @param googleUserAttributes Google에서 제공하는 사용자 속성 정보
     */
    public GoogleOAuth2Response(Map<String, Object> googleUserAttributes) {
        this.googleUserAttributes = googleUserAttributes;
    }

    /**
     * OAuth2 제공자 이름을 반환합니다.
     *
     * @return "google" 문자열
     */
    @Override
    public String getOauth2ProviderName() {
        return "google";
    }

    /**
     * Google에서 제공하는 사용자 고유 식별자를 반환합니다.
     *
     * @return Google 사용자의 sub(subject) 값
     */
    @Override
    public String getOauth2ProviderId() {
        return googleUserAttributes.get("sub").toString();
    }

    /**
     * 사용자의 이메일 주소를 반환합니다.
     *
     * @return Google 계정의 이메일 주소
     */
    @Override
    public String getUserEmailAddress() {
        return googleUserAttributes.get("email").toString();
    }

    /**
     * 사용자의 이름을 반환합니다.
     *
     * @return Google 계정의 사용자 이름
     */
    @Override
    public String getUserDisplayName() {
        return googleUserAttributes.get("name").toString();
    }

    /**
     * 사용자의 프로필 이미지 URL을 반환합니다.
     *
     * @return Google 계정의 프로필 이미지 URL, 없으면 null
     */
    @Override
    public String getUserProfileImageUrl() {
        return googleUserAttributes.get("picture") != null ?
                googleUserAttributes.get("picture").toString() : null;
    }
}