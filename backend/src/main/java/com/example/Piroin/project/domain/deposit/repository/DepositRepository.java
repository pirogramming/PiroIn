package com.example.Piroin.project.domain.deposit.repository;

import com.example.Piroin.project.domain.deposit.entity.Deposit;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    Optional<Deposit> findByUser(User user);
}
