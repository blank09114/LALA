package com.example.lala.JPARepository;

import com.example.lala.Entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Integer> {

    // JpaRepository가 기본적인 CRUD 기능을 이미 다 제공
    // JpaRepository를 상속하면 findById(), findAll(), save(), deleteById() 같은 메서드는 자동으로 제공돼서 구현할 필요 없다.

}
