package com.example.Piroin.project.domain.user.repository;

import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
