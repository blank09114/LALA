package com.example.lala.Config;

import com.example.lala.Entity.ActiveType;
import com.example.lala.Entity.Role;
import com.example.lala.Entity.User;
import com.example.lala.Entity.UserType;
import com.example.lala.JPARepository.ActiveTypeRepository;
import com.example.lala.JPARepository.JPAUserRepository;
import com.example.lala.JPARepository.RoleRepository;
import com.example.lala.JPARepository.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * 애플리케이션 초기 데이터 설정 클래스
 * <p>
 * Spring Boot 애플리케이션 시작 시 자동으로 실행되어 기본 관리자 계정을 생성합니다.
 * CommandLineRunner 인터페이스를 구현하여 애플리케이션 시작 후 한 번만 실행됩니다.
 * <p>
 * 주요 기능:
 * - 기본 관리자 계정 자동 생성
 * - 중복 생성 방지를 위한 플래그 파일 관리
 * - 필요한 기본 엔티티(Role, UserType, ActiveType) 설정
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JPAUserRepository JPAUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTypeRepository userTypeRepository;
    private final ActiveTypeRepository activeTypeRepository;

    /**
     * 관리자 계정 생성 완료 여부를 확인하는 플래그 파일 경로
     * 이 파일이 존재하면 관리자 계정이 이미 생성되었음을 의미합니다.
     */
    private static final String FLAG_PATH = "admin_created.flag";

    /**
     * 애플리케이션 시작 시 자동으로 실행되는 메서드
     * <p>
     * 실행 순서:
     * 1. 플래그 파일 존재 여부 확인 (중복 실행 방지)
     * 2. 관리자 이메일 중복 체크
     * 3. 필요한 엔티티들(Role, UserType, ActiveType) 조회 또는 생성
     * 4. 관리자 계정 생성
     * 5. 플래그 파일 생성
     *
     * @param args 명령행 인수 (사용하지 않음)
     * @throws Exception 파일 생성 또는 데이터베이스 작업 중 발생할 수 있는 예외
     */
    @Override
    public void run(String... args) throws Exception {

        if (Files.exists(Path.of(FLAG_PATH))) {
            System.out.println("관리자 계정 생성 이미 완료됨.");
            return;
        }

        if (JPAUserRepository.findByEmail("admin@admin.com").isEmpty()) {

            Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            UserType adminType = userTypeRepository.findById(3)
                    .orElseThrow(() -> new IllegalArgumentException("관리자 타입 없음"));

            ActiveType adminActiveType = activeTypeRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("활동 타입 없음"));

            User admin = User.builder()
                    .email("admin@admin.com")
                    .userNickname("관리자")
                    .userPassword(passwordEncoder.encode("1234"))
                    .userRole(adminRole)
                    .membershipRegistrationDate(LocalDate.now())
                    .accountType(adminType)
                    .activityType(adminActiveType)
                    .build();

            JPAUserRepository.save(admin);
            Files.createFile(Path.of(FLAG_PATH));
            System.out.println("관리자 계정 생성 완료");
        }
    }
}