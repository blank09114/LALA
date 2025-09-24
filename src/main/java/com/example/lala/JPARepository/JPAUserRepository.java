package com.example.lala.JPARepository;

import com.example.lala.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JPAUserRepository extends JpaRepository<User, String> {

    // 이메일로 사용자 찾기 (필드명 수정)
    Optional<User> findByEmail(String email);

    // 소셜 로그인 사용자 식별
    Optional<User> findBySocialLoginIdAndAuthenticationProvider(String socialLoginId, String authenticationProvider);

    // 닉네임과 이메일 존재 확인 (필드명 수정)
    boolean existsByUserNickname(String userNickname);
    boolean existsByEmail(String email);





}