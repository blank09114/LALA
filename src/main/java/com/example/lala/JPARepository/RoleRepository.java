package com.example.lala.JPARepository;


import com.example.lala.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Optional :
    // Null 일 수 있는 객체나 값을 감싸주는 래퍼 클래스로, Optional 안의 value 값은 제네릭 타입의 Null 일수도 있는 하나의 값이다.
    // Optional 클래스는 value 값을 다양한 메서드를 통해 접근하도록 하고, 이로 인해 NPE가 발생될 여지를 줄여준다.
    // 주로 메서드로는 Optional 객체 생성 메서드, 비어있는지 확인하는 메서드, 값을 꺼내는 메서드, 중간 연산 메서드가 있음
    //
    Optional<Role> findByRoleName(String name);

    /**
     * 권한명의 존재 여부를 확인합니다.
     *
     * @param roleName 확인할 권한명
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByRoleName(String roleName);
}


