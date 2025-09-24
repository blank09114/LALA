package com.example.lala.Service;

import com.example.lala.DTO.Oauth.CustomOAuth2UserImpl;
import com.example.lala.DTO.Oauth.GoogleOAuth2Response;
import com.example.lala.DTO.Oauth.KakaoOAuth2Response;
import com.example.lala.DTO.Oauth.OAuth2UserResponse;
import com.example.lala.Entity.ActiveType;
import com.example.lala.Entity.Role;
import com.example.lala.Entity.User;
import com.example.lala.Entity.UserType;
import com.example.lala.JPARepository.ActiveTypeRepository;
import com.example.lala.JPARepository.JPAUserRepository;
import com.example.lala.JPARepository.RoleRepository;
import com.example.lala.JPARepository.UserTypeRepository;
import com.example.lala.exception.ProviderMismatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * OAuth2 소셜 로그인 처리를 위한 커스텀 사용자 서비스
 * Google, Kakao 등의 OAuth2 제공자로부터 받은 사용자 정보를 처리하여
 * 애플리케이션의 사용자 엔티티로 변환하고 저장하는 역할을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final JPAUserRepository JPAUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final ActiveTypeRepository activeTypeRepository;
    private final RoleRepository roleRepository;

    /**
     * OAuth2 사용자 정보를 로드하고 처리하는 메인 메서드
     *
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 처리된 OAuth2 사용자 객체
     * @throws OAuth2AuthenticationException OAuth2 인증 과정에서 발생하는 예외
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 상위 클래스에서 기본 OAuth2 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User attribute : {}", oAuth2User.getAttributes());

        // OAuth2 제공자 식별자 추출 (kakao, google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 제공자별로 사용자 응답 객체를 생성
        OAuth2UserResponse OAuth2UserResponse = createOAuth2UserResponse(registrationId, oAuth2User);

        // 사용자 정보 추출
        String email = OAuth2UserResponse.getUserEmailAddress();
        String provider = OAuth2UserResponse.getOauth2ProviderName();
        String providerId = OAuth2UserResponse.getOauth2ProviderId();
        String nickname = OAuth2UserResponse.getUserDisplayName();

        // 이메일로 기존 사용자 조회
        Optional<User> userOptional = JPAUserRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            // 기존 사용자가 존재하는 경우
            user = userOptional.get();

            // OAuth2 제공자 불일치 검증
            validateProviderConsistency(user, provider);
        } else {
            // 새로운 사용자 등록
            user = createNewUser(email, nickname, provider, providerId);

            // 사용자 등록 정보 로깅
            log.info("가입 데이터: email={}, provider={}, providerId={}, nickname={}",
                    email, provider, providerId, nickname);

            user = JPAUserRepository.save(user);
        }

        // ✅ CustomOAuth2UserImpl 생성 시 실제 DB UUID 설정
        CustomOAuth2UserImpl customUser = new CustomOAuth2UserImpl(OAuth2UserResponse);
        customUser.setDatabaseUserId(user.getUserId()); // 실제 DB UUID 설정

        log.info("OAuth2 사용자 로그인 완료: email={}, databaseUserId={}", email, user.getUserId());

        return customUser;
    }

    /**
     * OAuth2 제공자별 사용자 응답 객체를 생성합니다.
     *
     * @param registrationId OAuth2 제공자 식별자
     * @param oAuth2User OAuth2 사용자 정보
     * @return 표준화된 OAuth2 사용자 응답 객체
     * @throws OAuth2AuthenticationException 지원하지 않는 제공자인 경우
     */
    private OAuth2UserResponse createOAuth2UserResponse(String registrationId, OAuth2User oAuth2User) {
        switch (registrationId) {
            case "kakao":
                return new KakaoOAuth2Response(oAuth2User.getAttributes());
            case "google":
                return new GoogleOAuth2Response(oAuth2User.getAttributes());
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
    }

    /**
     * 기존 사용자의 OAuth2 제공자 일치성을 검증합니다.
     *
     * @param user 기존 사용자
     * @param currentProvider 현재 로그인 시도 제공자
     * @throws ProviderMismatchException 제공자가 일치하지 않는 경우
     */
    private void validateProviderConsistency(User user, String currentProvider) {
        if (!user.getAuthenticationProvider().equals(currentProvider)) {
            throw new ProviderMismatchException(
                    "이미 " + user.getAuthenticationProvider() + " 계정으로 가입된 이메일입니다. " +
                            user.getAuthenticationProvider() + "로 로그인해주세요."
            );
        }
    }

    /**
     * 새로운 OAuth2 사용자를 생성합니다.
     *
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param provider OAuth2 제공자
     * @param providerId OAuth2 제공자에서의 사용자 ID
     * @return 생성된 사용자 엔티티
     */
    private User createNewUser(String email, String nickname, String provider, String providerId) {
        // 기본 사용자 타입 조회 (ID: 1번)
        UserType defaultUserType = userTypeRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("기본 UserType이 존재하지 않습니다."));

        // 기본 활동 타입 조회 (ID: 1번)
        ActiveType defaultActiveType = activeTypeRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("기본 ActiveType이 존재하지 않습니다."));

        // 기본 사용자 권한 조회 또는 생성
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName("ROLE_USER").build()
                ));

        // 새 사용자 엔티티 생성
        return User.builder()
                .email(email)
                .userNickname(nickname)
                .authenticationProvider(provider)
                .socialLoginId(providerId) // ✅ socialLoginId 필드에 providerId 저장
                .userPassword("{noop}oauth2_user") // OAuth2 사용자는 패스워드 불필요
                .membershipRegistrationDate(LocalDate.now())
                .activityType(defaultActiveType)
                .accountType(defaultUserType)
                .userRole(userRole)
                .build();
    }
}