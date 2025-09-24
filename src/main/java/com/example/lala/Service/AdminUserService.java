package com.example.lala.Service;

import com.example.lala.DTO.UserDTO;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 권한의 사용자 관리 기능을 제공하는 서비스 인터페이스
 * 사용자 조회, 수정, 필터링, 검색 등의 기능을 정의합니다.
 */
public interface AdminUserService {

    /**
     * 전체 사용자 목록을 조회합니다.
     * 
     * @return 모든 사용자 정보 목록
     */
    List<UserDTO> findAllUsers();

    /**
     * 특정 사용자의 유형을 변경합니다.
     *
     * @param userId        변경할 사용자의 고유 식별자
     * @param newUserTypeNo 새로운 사용자 유형 번호
     * @return
     */
    void updateUserType(String userId, int newUserTypeNo);

    /**
     * 특정 사용자의 활성화 상태를 변경합니다.
     *
     * @param userId          변경할 사용자의 고유 식별자
     * @param newActiveTypeNo 새로운 활성화 상태 번호
     * @return
     */
    void updateActiveType(String userId, int newActiveTypeNo);

    /**
     * 사용자 ID로 특정 사용자 정보를 조회합니다.
     * 
     * @param userId 조회할 사용자의 고유 식별자
     * @return 사용자 정보 (존재하지 않으면 Optional.empty())
     */
    Optional<UserDTO> findUserById(String userId);

}