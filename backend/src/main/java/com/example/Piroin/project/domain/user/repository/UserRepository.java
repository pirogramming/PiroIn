package com.example.Piroin.project.domain.user.repository;

import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);

    List<User> findByRole(Role role);

    // 학생 이름으로 검색기능
    List<User> findByNameContainingAndRole(String name, Role role);
}
