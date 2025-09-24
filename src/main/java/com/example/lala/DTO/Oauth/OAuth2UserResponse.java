package com.example.lala.DTO.Oauth;

/**
 * OAuth2 제공업체의 사용자 정보 응답을 표준화하는 인터페이스
 * 다양한 OAuth2 제공업체(Google, Kakao, Naver 등)의 응답을 통일된 형태로 처리하기 위해 사용됩니다.
 */
public interface OAuth2UserResponse {

    /**
     * OAuth2 제공업체명을 반환합니다.
     * @return OAuth2 제공업체 이름 (예: "google", "kakao")
     */
    String getOauth2ProviderName();

    /**
     * OAuth2 제공업체에서 발급한 사용자 고유 식별자를 반환합니다.
     * @return 제공업체별 사용자 고유 ID
     */
    String getOauth2ProviderId();

    /**
     * 사용자의 이메일 주소를 반환합니다.
     * @return 사용자 이메일 주소
     */
    String getUserEmailAddress();

    /**
     * 사용자의 실명 또는 설정된 이름을 반환합니다.
     * @return 사용자 이름
     */
    String getUserDisplayName();

    /**
     * 사용자의 프로필 이미지 URL을 반환합니다.
     * @return 프로필 이미지 URL
     */
    String getUserProfileImageUrl();
}