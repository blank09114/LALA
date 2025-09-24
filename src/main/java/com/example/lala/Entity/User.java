package com.example.lala.Entity;

import com.example.lala.DTO.UserDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 사용자 정보를 저장하는 JPA 엔티티 클래스
 * 시스템의 회원 정보, 인증 정보, 소셜 로그인 정보를 관리합니다.
 * 일반 회원가입과 OAuth2 소셜 로그인을 모두 지원합니다.
 */
@Entity
@Table(name = "`user`")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 사용자 고유 식별자 (UUID 형태의 기본키)
     */
    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String userId;

    /**
     * 엔티티가 처음 저장되기 전에 호출되어 필수 값들을 초기화합니다.
     */
    @PrePersist
    public void prePersist() {
        this.userId = UUID.randomUUID().toString();
        this.membershipRegistrationDate = LocalDate.now();
    }

    /**
     * 사용자 이메일 주소 (로그인 ID로 사용)
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * 사용자 비밀번호 (암호화되어 저장)
     */
    @Column(name = "password", nullable = false)
    private String userPassword;

    /**
     * 사용자 닉네임
     */
    @Column(name = "nickname", nullable = false)
    private String userNickname;

    /**
     * 회원가입 날짜
     */
    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDate membershipRegistrationDate;

    /**
     * 비밀번호 최종 변경 일자
     */
    @Column(name = "password_update")
    private LocalDate passwordLastUpdateDate;

    /**
     * 프로필 이미지 파일명
     */
    @Column(name = "profilename")
    private String profileImageFileName;

    /**
     * 소셜 로그인 고유 ID (OAuth2 providerId)
     */
    private String socialLoginId;

    /**
     * 로그인 제공자 (LALA, google, kakao 등)
     */
    @Builder.Default
    @Column(name = "provider")
    private String authenticationProvider = "LALA";

    /**
     * 사용자 권한 역할
     */
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role userRole;

    /**
     * 사용자 계정 유형
     */
    @ManyToOne
    @JoinColumn(name = "user_type", referencedColumnName = "no", nullable = false)
    private UserType accountType;

    /**
     * 사용자 활동 유형
     */
    @ManyToOne
    @JoinColumn(name = "active_type", referencedColumnName = "no", nullable = false)
    private ActiveType activityType;

    /**
     * UserDTO를 기반으로 User 엔티티 객체를 생성하는 정적 팩토리 메서드
     * 회원가입 시 사용되며, 필요한 기본값들을 자동으로 설정합니다.
     *
     * @param userDTO             회원가입 폼 데이터를 담은 DTO
     * @param passwordEncoder     비밀번호 암호화를 위한 인코더
     * @param defaultAccountType  기본 계정 유형
     * @param defaultActivityType 기본 활동 유형
     * @param userRole            사용자 권한 역할
     * @return 생성된 User 엔티티 객체
     */
    public static User create(UserDTO userDTO, PasswordEncoder passwordEncoder,
                              UserType defaultAccountType, ActiveType defaultActivityType, Role userRole) {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail(userDTO.getEmail());
        user.setUserNickname(userDTO.getUserNickname());
        user.setUserPassword(passwordEncoder.encode(userDTO.getUserPassword()));
        user.setAccountType(defaultAccountType);
        user.setActivityType(defaultActivityType);
        user.setMembershipRegistrationDate(LocalDate.now());
        user.setProfileImageFileName("defaultProfile.png");
        user.setUserRole(userRole);
        return user;
    }
}