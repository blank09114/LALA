package com.example.lala.DTO.Auth;

import com.example.lala.Entity.Role;
import com.example.lala.Entity.UserType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Spring Security의 인증된 사용자 정보를 담는 커스텀 UserDetails 구현체
 * 기본 UserDetails 인터페이스에서 제공하는 정보 외에
 * 애플리케이션에 필요한 추가 사용자 정보를 포함합니다.
 */
public class CustomUserDetailsImpl implements UserDetails {

    /**
     * 사용자 이메일 주소 (로그인 아이디로 사용)
     */
    private final String userEmail;

    /**
     * 사용자 비밀번호
     */
    private final String userPassword;

    /**
     * 사용자 닉네임
     */
    @Getter
    private final String userNickname;

    /**
     * 사용자 고유 식별자
     */
    @Getter
    private final String uniqueUserId;

    /**
     * 사용자 역할/권한
     */
    private final Role userRole;

    @Getter
    private final UserType userType;

    /**
     * Spring Security에서 사용하는 권한 컬렉션
     */
    private final Collection<? extends GrantedAuthority> grantedAuthorities;

    /**
     * CustomUserDetails 생성자
     *
     * @param userEmail    사용자 이메일
     * @param userPassword 사용자 비밀번호
     * @param userNickname 사용자 닉네임
     * @param uniqueUserId 사용자 고유 ID
     * @param userRole     사용자 역할
     * @param userType     사용자 타입
     */
    public CustomUserDetailsImpl(String userEmail, String userPassword, String userNickname, String uniqueUserId, Role userRole, UserType userType) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userNickname = userNickname;
        this.uniqueUserId = uniqueUserId;
        this.userRole = userRole;
        this.userType = userType;

        // ✅ 권한 설정 로직 수정 - 먼저 권한을 구성한 후 할당
        this.grantedAuthorities = createAuthorities(userRole, userType);
    }

    public String getUserId() {
        return uniqueUserId;
    }


    private Collection<? extends GrantedAuthority> createAuthorities(Role userRole, UserType userType) {
        var authorities = new ArrayList<GrantedAuthority>();

        // 기본 Role 권한 추가
        authorities.add(new SimpleGrantedAuthority(userRole.getRoleName()));

        // UserType에 따른 추가 권한 부여 (필요한 경우에만)
        switch (userType.getAccountTypeId()) {
            case 0:
                // 일반 사용자 - ROLE_USER만 (기본 Role에서 이미 추가됨)
                System.out.println("[DEBUG] 일반 사용자 권한: ROLE_USER");
                break;
            case 1:
                // 지식 제공자 - 이미 ROLE_PROVIDER가 기본 Role로 설정됨
                // 필요시 ROLE_USER도 추가 (선택사항)
                if (!"ROLE_USER".equals(userRole.getRoleName())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                System.out.println("[DEBUG] 지식제공자 권한: " + userRole.getRoleName() + " + ROLE_USER");
                break;
            case 2:
                // 지식제공자 요청 - ROLE_USER만 (기본 Role에서 이미 추가됨)
                System.out.println("[DEBUG] 지식제공자 요청 권한: ROLE_USER");
                break;
            case 3:
                // 관리자 - 필요시 다른 권한들도 추가 가능
                if (!"ROLE_USER".equals(userRole.getRoleName())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                System.out.println("[DEBUG] 관리자 권한: " + userRole.getRoleName() + " + ROLE_USER");
                break;
            default:
                System.out.println("[DEBUG] 알 수 없는 사용자 타입: " + userType.getAccountTypeId());
        }

        System.out.println("[DEBUG] 최종 권한 목록: " + authorities);
        return authorities;
    }

    /**
     * 사용자의 권한 목록을 반환합니다.
     *
     * @return 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     *
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        return userPassword;
    }

    /**
     * 사용자의 로그인 아이디(이메일)를 반환합니다.
     *
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return userEmail;
    }

    /**
     * 계정이 만료되지 않았는지 확인합니다.
     *
     * @return 항상 true (계정 만료 없음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정이 잠금되지 않았는지 확인합니다.
     *
     * @return 항상 true (계정 잠금 없음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명이 만료되지 않았는지 확인합니다.
     *
     * @return 항상 true (자격 증명 만료 없음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정이 활성화되어 있는지 확인합니다.
     *
     * @return 항상 true (계정 활성화)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}