package com.example.lala.Service;

import com.example.lala.DTO.UserDTO;
import com.example.lala.Entity.User;

import java.util.Optional;

/**
 * 사용자 관리 서비스 인터페이스
 * 사용자 회원가입, 조회, 중복 검증 등 사용자 계정과 관련된 핵심 비즈니스 로직을 정의합니다.
 * 일반 회원가입과 소셜 로그인을 통한 사용자 관리 기능을 제공합니다.
 */
public interface UserService {

    /**
     * 새로운 사용자를 시스템에 등록합니다.
     * 사용자 정보 유효성 검증, 비밀번호 암호화, 기본 권한 설정 등의 과정을 거쳐
     * 데이터베이스에 새로운 사용자 정보를 저장합니다.
     * 
     * @param userDTO 회원가입에 필요한 사용자 정보 (이메일, 비밀번호, 닉네임 등)
     * @throws RuntimeException 이메일 중복, 유효성 검증 실패 등의 경우 발생
     */
    void signUp(UserDTO userDTO);

    /**
     * 이메일 주소를 통해 사용자 정보를 조회합니다.
     * 로그인 인증, 비밀번호 재설정, 사용자 정보 확인 등에서 사용됩니다.
     * 
     * @param email 조회할 사용자의 이메일 주소
     * @return 해당 이메일의 사용자 정보를 담은 Optional 객체
     *         사용자가 존재하지 않으면 Optional.empty() 반환
     */
    Optional<User> findByEmail(String email);

    /**
     * 특정 이메일 주소가 이미 시스템에 등록되어 있는지 확인합니다.
     * 회원가입 시 이메일 중복 검증과 사용자 존재 여부 확인에 사용됩니다.
     * 
     * @param email 중복 검사할 이메일 주소
     * @return 이메일이 이미 등록되어 있으면 true, 그렇지 않으면 false
     */
    boolean existsByEmail(String email);
}