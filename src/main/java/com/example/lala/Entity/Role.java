package com.example.lala.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 권한 역할을 나타내는 JPA 엔티티 클래스
 * 시스템에서 사용자의 권한 레벨을 정의하기 위한 역할 정보를 관리합니다.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    /**
     * 역할의 고유 식별자 (기본키)
     * 데이터베이스에서 자동 증가되는 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    /**
     * 역할의 명칭
     */
    @Column(name = "name", nullable = false)
    private String roleName;

    /**
     * 역할명만으로 Role 객체를 생성하는 생성자
     * @param roleName 생성할 역할의 명칭
     */
    public Role(String roleName) {
        this.roleName = roleName;
    }
}