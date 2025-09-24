package com.example.lala.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * 사용자 계정 유형 정보를 나타내는 JPA 엔티티 클래스
 * 사용자의 계정 구분을 위한 코드성 데이터를 관리합니다.
 * 예: 일반 사용자, 프리미엄 사용자, 강사 등의 계정 유형 정보
 */
@Entity
@Table(name = "user_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserType {

    /**
     * 사용자 계정 유형의 고유 식별자 (기본키)
     */
    @Id
    @Column(name = "no")
    private int accountTypeId;

    /**
     * 사용자 계정 유형의 명칭 (예: 일반 사용자, 프리미엄 사용자, 강사)
     */
    @Column(name = "account_type_name", nullable = false)
    private String accountTypeName;
}