package com.example.lala.Service;

import com.example.lala.DTO.UserDTO;
import com.example.lala.Entity.ActiveType;
import com.example.lala.Entity.Role;
import com.example.lala.Entity.User;
import com.example.lala.Entity.UserType;
import com.example.lala.JPARepository.ActiveTypeRepository;
import com.example.lala.JPARepository.JPAUserRepository;
import com.example.lala.JPARepository.RoleRepository;
import com.example.lala.JPARepository.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 관리자 전용 사용자 관리 서비스 구현 클래스
 * 사용자 조회, 사용자 유형 변경, 활동 상태 변경 등의 관리자 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 사용자 전체 조회 및 검색
 * - 사용자 유형별, 활동 상태별 필터링
 * - 사용자 계정 유형 및 활동 유형 변경
 * - 복합 조건을 통한 사용자 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    /**
     * 사용자 정보를 관리하는 JPA Repository
     */
    private final JPAUserRepository JPAUserRepository;

    /**
     * 사용자 계정 유형을 관리하는 JPA Repository
     */
    private final UserTypeRepository userTypeRepository;

    /**
     * 사용자 활동 유형을 관리하는 JPA Repository
     */
    private final ActiveTypeRepository activeTypeRepository;

    /**
     * 사용자 권한을 관리하는 JPA Repository
     */
    private final RoleRepository roleRepository;

    private final AdminProviderService adminProviderService;


    /**
     * 전체 사용자 목록을 조회합니다.
     *
     * @return 전체 사용자 정보를 담은 UserDTO 리스트
     */
    @Override
    public List<UserDTO> findAllUsers() {
        return JPAUserRepository.findAll().stream()
                .map(UserDTO::of)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 계정 유형을 변경하고 적절한 권한을 자동으로 설정합니다.
     * 트랜잭션을 통해 데이터 일관성을 보장합니다.
     *
     * @param userId        변경할 사용자의 ID
     * @param newUserTypeNo 새로운 계정 유형 번호
     * @throws RuntimeException 해당 사용자, 계정 유형 또는 권한이 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void updateUserType(String userId, int newUserTypeNo) {
        // 사용자 조회
        User user = JPAUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));

        // 새로운 사용자 유형 조회
        UserType newUserType = userTypeRepository.findById(newUserTypeNo)
                .orElseThrow(() -> new RuntimeException("해당 회원 유형이 없습니다."));

        // 기존 정보 로깅
        String oldUserTypeName = user.getAccountType() != null ?
                user.getAccountType().getAccountTypeName() : "없음";
        String oldRoleName = user.getUserRole() != null ?
                user.getUserRole().getRoleName() : "없음";
        int oldUserTypeId = user.getAccountType() != null ?
                user.getAccountType().getAccountTypeId() : -1;

        // 사용자 유형에 따른 적절한 권한 설정
        Role newRole = getRoleByUserType(newUserTypeNo);

        // 사용자 정보 업데이트
        user.setAccountType(newUserType);
        user.setUserRole(newRole);

        JPAUserRepository.save(user);

        // 지식제공자 요청에서 승인으로 변경된 경우 approval_date 업데이트
        if (oldUserTypeId == 2 && newUserTypeNo == 1) {
            try {
                // AdminProviderService를 주입받아야 함
                adminProviderService.approveProviderRequest(userId);
                log.info("지식제공자 요청 승인 처리 완료 - userId: {}", userId);
            } catch (Exception e) {
                log.error("지식제공자 요청 승인 처리 실패 - userId: {}, error: {}",
                        userId, e.getMessage());
                // 사용자 유형은 이미 변경되었으므로 경고만 로그
            }
        }
// 지식제공자 요청에서 일반회원으로 변경된 경우 (거부)
        if (oldUserTypeId == 2 && newUserTypeNo == 0) {
            try {
                adminProviderService.rejectProviderRequest(userId);
                log.info("지식제공자 요청 거부 처리 완료 - userId: {}", userId);
            } catch (Exception e) {
                log.error("지식제공자 요청 거부 처리 실패 - userId: {}, error: {}",
                        userId, e.getMessage());
            }
        }

        // 변경 내역 로깅
        log.info("사용자 정보 변경 완료 - userId: {}, " +
                        "계정유형: {} → {}, " +
                        "권한: {} → {}",
                userId,
                oldUserTypeName, newUserType.getAccountTypeName(),
                oldRoleName, newRole.getRoleName());
    }
    /**
     * 사용자 유형에 따른 적절한 Role을 반환하는 매핑 메서드
     */
    private Role getRoleByUserType(int userTypeNo) {
        try {
            String roleName = switch (userTypeNo) {
                case 0 -> "ROLE_USER";
                case 1 -> "ROLE_PROVIDER";
                case 2 -> "ROLE_USER";
                case 3 -> "ROLE_ADMIN";
                default -> {
                    log.warn("알 수 없는 사용자 유형: {}. 기본 권한(ROLE_USER)을 할당합니다.", userTypeNo);
                    yield "ROLE_USER";
                }
            };

            log.info("권한 조회 시도 - userTypeNo: {}, 찾을 roleName: {}", userTypeNo, roleName);

            // 모든 권한 목록 먼저 로그 출력
            List<Role> allRoles = roleRepository.findAll();
            log.info("현재 데이터베이스에 존재하는 모든 권한: {}",
                    allRoles.stream().map(Role::getRoleName).collect(Collectors.toList()));

            Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);

            if (roleOptional.isEmpty()) {
                log.error("권한을 찾을 수 없음 - roleName: {}", roleName);
                throw new RuntimeException("권한을 찾을 수 없습니다: " + roleName +
                        ". 현재 존재하는 권한: " + allRoles.stream().map(Role::getRoleName).collect(Collectors.toList()));
            }

            Role role = roleOptional.get();
            log.info("권한 조회 성공 - roleId: {}, roleName: {}", role.getRoleId(), role.getRoleName());
            return role;

        } catch (Exception e) {
            log.error("권한 조회 중 오류 발생 - userTypeNo: {}, error: {}", userTypeNo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 특정 사용자의 활동 유형을 변경합니다.
     * 트랜잭션을 통해 데이터 일관성을 보장합니다.
     *
     * @param userId          변경할 사용자의 ID
     * @param newActiveTypeNo 새로운 활동 유형 번호
     * @throws RuntimeException 해당 사용자 또는 활동 유형이 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void updateActiveType(String userId, int newActiveTypeNo) {
        User user = JPAUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));
        ActiveType newActivityType = activeTypeRepository.findById(newActiveTypeNo)
                .orElseThrow(() -> new RuntimeException("해당 활동 유형이 없습니다."));

        String oldActivityTypeName = user.getActivityType() != null ?
                user.getActivityType().getActivityTypeName() : "없음";

        user.setActivityType(newActivityType);
        JPAUserRepository.save(user);

        log.info("사용자 활동유형 변경 완료 - userId: {}, 활동유형: {} → {}",
                userId, oldActivityTypeName, newActivityType.getActivityTypeName());
    }

    /**
     * 특정 ID의 사용자 정보를 조회합니다.
     */
    @Override
    public Optional<UserDTO> findUserById(String userId) {
        try {
            log.info("사용자 조회 시작 - userId: {}", userId);

            Optional<User> userOptional = JPAUserRepository.findById(userId);

            if (userOptional.isEmpty()) {
                log.warn("사용자를 찾을 수 없음 - userId: {}", userId);
                return Optional.empty();
            }

            User user = userOptional.get();
            log.info("사용자 엔티티 조회 성공 - userId: {}, email: {}, nickname: {}",
                    userId, user.getEmail(), user.getUserNickname());

            UserDTO userDTO = UserDTO.of(user);
            log.info("UserDTO 변환 완료 - userId: {}, email: {}, nickname: {}",
                    userDTO.getUserId(), userDTO.getEmail(), userDTO.getUserNickname());

            return Optional.of(userDTO);

        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("사용자 조회 중 오류 발생", e);
        }
    }
}