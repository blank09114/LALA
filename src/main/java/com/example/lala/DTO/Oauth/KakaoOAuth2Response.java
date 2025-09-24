package com.example.lala.DTO.Oauth;

import java.util.Map;

/**
 * Kakao OAuth2 로그인 응답 정보를 처리하는 클래스
 * Kakao에서 제공하는 사용자 정보를 파싱하여 표준화된 형태로 변환합니다.
 */
public class KakaoOAuth2Response implements OAuth2UserResponse {

    /**
     * Kakao OAuth2 API로부터 받은 사용자 속성 정보
     */
    private final Map<String, Object> kakaoUserAttributes;

    /**
     * KakaoResponse 생성자
     *
     * @param kakaoUserAttributes Kakao에서 제공하는 사용자 속성 정보
     */
    public KakaoOAuth2Response(Map<String, Object> kakaoUserAttributes) {
        this.kakaoUserAttributes = kakaoUserAttributes;
    }

    /**
     * OAuth2 제공자 이름을 반환합니다.
     *
     * @return "kakao" 문자열
     */
    @Override
    public String getOauth2ProviderName() {
        return "kakao";
    }

    /**
     * Kakao에서 제공하는 사용자 고유 식별자를 반환합니다.
     *
     * @return Kakao 사용자의 ID 값
     */
    @Override
    public String getOauth2ProviderId() {
        return String.valueOf(kakaoUserAttributes.get("id"));
    }

    /**
     * 사용자의 이메일 주소를 반환합니다.
     *
     * @return Kakao 계정의 이메일 주소, 없으면 null
     */
    @Override
    public String getUserEmailAddress() {
        Object kakaoAccountObj = kakaoUserAttributes.get("kakao_account");
        if (kakaoAccountObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccountInfo = (Map<String, Object>) kakaoAccountObj;
            return (String) kakaoAccountInfo.get("email");
        }
        return null;
    }

    /**
     * 사용자의 닉네임을 반환합니다.
     *
     * @return Kakao 계정의 프로필 닉네임, 없으면 null
     */
    @Override
    public String getUserDisplayName() {
        Object kakaoAccountObj = kakaoUserAttributes.get("kakao_account");
        if (kakaoAccountObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccountInfo = (Map<String, Object>) kakaoAccountObj;
            Object userProfileObj = kakaoAccountInfo.get("profile");
            if (userProfileObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userProfile = (Map<String, Object>) userProfileObj;
                return (String) userProfile.get("nickname");
            }
        }
        return null;
    }

    /**
     * 사용자의 프로필 이미지 URL을 반환합니다.
     *
     * @return Kakao 계정의 프로필 이미지 URL, 없으면 null
     */
    @Override
    public String getUserProfileImageUrl() {
        Object kakaoAccountObj = kakaoUserAttributes.get("kakao_account");
        if (kakaoAccountObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccountInfo = (Map<String, Object>) kakaoAccountObj;
            Object userProfileObj = kakaoAccountInfo.get("profile");
            if (userProfileObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userProfile = (Map<String, Object>) userProfileObj;
                return (String) userProfile.get("profile_image_url");
            }
        }
        return null;
    }
}