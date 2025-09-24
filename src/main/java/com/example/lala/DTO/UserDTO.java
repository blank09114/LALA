package com.example.lala.DTO;

import com.example.lala.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 데이터 전송 객체 (DTO)
 * 회원가입, 로그인, 사용자 정보 조회 등에서 사용되는 사용자 관련 데이터를 전송하기 위한 클래스입니다.
 * User 엔티티와 컨트롤러 간의 데이터 전송을 담당합니다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    /**
     * 사용자 고유 식별자
     */
    private String userId;

    /**
     * 사용자 이메일 주소
     */
    private String email;

    /**
     * 사용자 비밀번호
     */
    private String userPassword;

    /**
     * 비밀번호 확인 (회원가입 시 사용)
     */
    private String passwordConfirmation;

    /**
     * 사용자 닉네임
     */
    private String userNickname;

    /**
     * 사용자 계정 유형 ID
     */
    private Integer accountTypeId;

    /**
     * 사용자 계정 유형명
     */
    private String accountTypeName;

    /**
     * 사용자 활동 유형 ID
     */
    private Integer activityTypeId;

    /**
     * 사용자 활동 유형명
     */
    private String activityTypeName;

    /**
     * 회원가입 날짜
     */
    private LocalDate membershipRegistrationDate;

    /*
    * 회원 권한 아이디
    * */
    private Long roleId;

    /**
     * User 엔티티를 UserDTO로 변환하는 정적 팩토리 메서드
     *
     * @param user 변환할 User 엔티티 객체
     * @return 변환된 UserDTO 객체
     */
    public static UserDTO of(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserId(user.getUserId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUserPassword(user.getUserPassword());
        userDTO.setUserNickname(user.getUserNickname());
        userDTO.setAccountTypeId(user.getAccountType() != null ? user.getAccountType().getAccountTypeId() : null);
        userDTO.setAccountTypeName(user.getAccountType() != null ? user.getAccountType().getAccountTypeName() : "알 수 없음");
        userDTO.setMembershipRegistrationDate(user.getMembershipRegistrationDate());
        userDTO.setActivityTypeId(user.getActivityType() != null ? user.getActivityType().getActivityTypeId() : null);
        userDTO.setActivityTypeName(user.getActivityType() != null ? user.getActivityType().getActivityTypeName() : "알 수 없음");

        // roleId 매핑 추가
        userDTO.setRoleId(user.getUserRole() != null ? user.getUserRole().getRoleId() : null);


        return userDTO;
    }
}