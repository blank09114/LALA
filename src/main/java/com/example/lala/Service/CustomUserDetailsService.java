package com.example.lala.Service;

import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import com.example.lala.Entity.Role;
import com.example.lala.Entity.User;
import com.example.lala.JPARepository.JPAUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Spring Security의 사용자 인증 정보 처리를 담당하는 커스텀 서비스
 * 일반 로그인(이메일/패스워드) 방식에서 사용자 정보를 데이터베이스에서 조회하고
 * Spring Security가 사용할 수 있는 UserDetails 객체로 변환하는 역할을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final JPAUserRepository JPAUserRepository;

    /**
     * 사용자 이메일을 기반으로 인증 정보를 로드합니다.
     * Spring Security가 로그인 처리 시 자동으로 호출하는 메서드입니다.
     *
     * @param email 로그인 시도 시 입력된 사용자 이메일
     * @return 인증에 필요한 사용자 상세 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 해당 이메일의 사용자가 존재하지 않을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("사용자 인증 정보 조회 시작 - 이메일: {}", email);

        // 데이터베이스에서 이메일로 사용자 조회
        Optional<User> userOptional = JPAUserRepository.findByEmail(email);

        // 사용자가 존재하지 않는 경우 예외 발생
        if (userOptional.isEmpty()) {
            log.warn("존재하지 않는 사용자 로그인 시도 - 이메일: {}", email);
            throw new UsernameNotFoundException("등록되지 않은 이메일입니다: " + email);
        }

        // 조회된 사용자 정보 추출
        User foundUser = userOptional.get();
        Role userRole = foundUser.getUserRole();

        log.info("사용자 인증 정보 조회 완료 - 사용자 ID: {}, 닉네임: {}",
                foundUser.getUserId(), foundUser.getUserNickname());

        // Spring Security에서 사용할 UserDetails 객체 생성 및 반환
        return new CustomUserDetailsImpl(
                foundUser.getEmail(),
                foundUser.getUserPassword(),
                foundUser.getUserNickname(),
                foundUser.getUserId(),
                foundUser.getUserRole(),
                foundUser.getAccountType()

        );
    }
}