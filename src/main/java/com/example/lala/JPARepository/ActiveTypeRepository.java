package com.example.lala.JPARepository;



import com.example.lala.Entity.ActiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveTypeRepository extends JpaRepository<ActiveType, Integer> {
}
