package com.example.lala.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * 사용자 활동 유형 정보를 나타내는 JPA 엔티티 클래스
 * 사용자의 활동 상태나 레벨을 분류하기 위한 코드성 데이터를 관리합니다.
 */
@Entity
@Table(name = "active_type")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveType {

    /**
     * 활동 유형의 고유 식별자 (기본키)
     */
    @Id
    @Column(name = "no")
    private int activityTypeId;

    /**
     * 활동 유형의 명칭
     */
    @Column(name = "activity_type_name", nullable = false)
    private String activityTypeName;
}