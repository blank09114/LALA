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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 사용자 관리 서비스 구현 클래스
 * UserService 인터페이스를 구현하여 일반 회원가입, 관리자 계정 생성, 사용자 조회 등
 * 사용자 계정과 관련된 핵심 비즈니스 로직을 처리합니다.
 * 
 * 주요 기능:
 * - 일반 사용자 회원가입 처리 (유효성 검증, 권한 설정, 암호화)
 * - 관리자 계정 생성 및 관리
 * - 사용자 정보 조회 및 중복 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * JPA 기반 사용자 데이터 액세스를 위한 Repository
     */
    private final JPAUserRepository JPAUserRepository;
    
    /**
     * 비밀번호 암호화를 위한 Spring Security 인코더
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 사용자 권한(Role) 관리를 위한 Repository
     */
    private final RoleRepository roleRepository;
    
    /**
     * 사용자 유형(UserType) 관리를 위한 Repository
     */
    private final UserTypeRepository userTypeRepository;
    
    /**
     * 사용자 활동 상태(ActiveType) 관리를 위한 Repository
     */
    private final ActiveTypeRepository activetypeRepository;

    /**
     * 새로운 사용자 회원가입을 처리합니다.
     * 
     * 처리 과정:
     * 1. 이메일 중복 검사
     * 2. 비밀번호 일치 검사
     * 3. 닉네임에 따른 권한 설정 (관리자/일반 사용자)
     * 4. 기본 사용자 타입 및 활동 상태 설정
     * 5. User 엔티티 생성 및 데이터베이스 저장
     * 
     * @param userDTO 회원가입에 필요한 사용자 정보 (이메일, 비밀번호, 닉네임 등)
     * @throws RuntimeException 이메일 중복, 비밀번호 불일치, 기본 데이터 누락 시 발생
     */
    @Override
    public void signUp(UserDTO userDTO) {
        
        // 이메일 중복 검사 - 동일한 이메일 주소로 가입된 계정이 있는지 확인
        if (JPAUserRepository.existsByEmail(userDTO.getEmail())) {
            log.warn("회원가입 실패: 이메일 중복 - {}", userDTO.getEmail());
            throw new RuntimeException("이메일이 존재합니다.");
        }

        // 비밀번호 일치 검사 - 입력한 비밀번호와 확인 비밀번호가 동일한지 검증
        // 주의: 원본 코드에서 동일한 필드를 비교하는 오류가 있었음 (수정 필요)
        if (!userDTO.getUserPassword().equals(userDTO.getPasswordConfirmation())) {
            log.warn("회원가입 실패: 비밀번호 불일치 - {}", userDTO.getEmail());
            throw new RuntimeException("비밀번호가 다릅니다.");
        }

        // 닉네임에 따른 권한 결정
        // "관리자" 닉네임이면 ROLE_ADMIN, 그 외에는 ROLE_USER 권한 부여
        String roleType = userDTO.getUserNickname().equals("관리자") ? "ROLE_ADMIN" : "ROLE_USER";

        // 해당 권한이 데이터베이스에 존재하지 않으면 새로 생성
        Role defaultRole = roleRepository.findByRoleName(roleType)
                .orElseGet(() -> {
                    log.info("새로운 권한 생성: {}", roleType);
                    return roleRepository.save(new Role(roleType));
                });

        // 기본 사용자 타입 조회 (ID: 0)
        UserType userType = userTypeRepository.findById(0)
                .orElseThrow(() -> new RuntimeException("기본 유저타입이 없습니다."));

        // 기본 활동 상태 조회 (ID: 1, 활성 상태)
        ActiveType activeType = activetypeRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("기본 활동 상태가 아닙니다."));

        // UserDTO를 User 엔티티로 변환하고 필요한 연관 엔티티들을 설정
        User user = User.create(userDTO, passwordEncoder, userType, activeType, defaultRole);
        user.setUserRole(defaultRole);
        
        // 최종적으로 사용자 정보를 데이터베이스에 저장
        JPAUserRepository.save(user);
        log.info("회원가입 성공: {} (권한: {})", userDTO.getEmail(), roleType);
    }

    /**
     * 이메일 주소를 통해 사용자 정보를 조회합니다.
     * 로그인 인증 시 사용자 존재 여부 확인과 사용자 정보 검색에 사용됩니다.
     * 
     * @param email 조회할 사용자의 이메일 주소
     * @return 해당 이메일의 사용자 정보를 담은 Optional 객체
     */
    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("이메일로 사용자 조회: {}", email);
        return JPAUserRepository.findByEmail(email);
    }

    /**
     * 특정 이메일 주소가 이미 시스템에 등록되어 있는지 확인합니다.
     * 회원가입 시 이메일 중복 검증에 사용됩니다.
     * 
     * @param email 중복 검사할 이메일 주소
     * @return 이메일이 이미 등록되어 있으면 true, 그렇지 않으면 false
     */
    @Override
    public boolean existsByEmail(String email) {
        boolean exists = JPAUserRepository.existsByEmail(email);
        log.debug("이메일 중복 검사 결과: {} = {}", email, exists);
        return exists;
    }

    /**
     * 시스템 관리자 계정을 생성합니다.
     * 
     * 관리자 계정 생성 과정:
     * 1. ROLE_ADMIN 권한 조회 또는 생성
     * 2. 관리자 사용자 타입 설정 (ID: 3)
     * 3. 활성 상태 설정
     * 4. Builder 패턴을 사용한 User 엔티티 생성
     * 5. 데이터베이스 저장
     * 
     * 중요: 모든 필수 필드(이메일, 닉네임, 비밀번호, 등록일 등)를 
     * 반드시 설정해야 데이터베이스 제약조건 오류를 방지할 수 있습니다.
     * 
     * @param userDto 관리자 계정 생성에 필요한 사용자 정보
     * @throws RuntimeException 관리자 타입 또는 활동 상태가 존재하지 않는 경우 발생
     */
    public void createAdminUser(UserDTO userDto) {
        
        // ROLE_ADMIN 권한 조회 또는 생성
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> {
                    log.info("ROLE_ADMIN 권한 생성");
                    return roleRepository.save(new Role("ROLE_ADMIN"));
                });

        // 관리자 사용자 타입 조회 (ID: 3)
        UserType adminType = userTypeRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("관리자 타입이 없습니다."));

        // 활성 상태 조회 (ID: 1)
        ActiveType activeType = activetypeRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("기본 활동 상태가 아닙니다."));

        // Builder 패턴을 사용하여 관리자 User 엔티티 생성
        // 모든 필수 필드를 명시적으로 설정하여 제약조건 오류 방지
        User adminUser = User.builder()
                .email(userDto.getEmail())
                .userNickname(userDto.getUserNickname())
                .userPassword(passwordEncoder.encode(userDto.getUserPassword()))
                .userRole(adminRole)
                .membershipRegistrationDate(LocalDate.now())
                .accountType(adminType)
                .activityType(activeType)
                .build();
                
        JPAUserRepository.save(adminUser);
        log.info("관리자 계정 생성 완료: {}", userDto.getEmail());
    }
}