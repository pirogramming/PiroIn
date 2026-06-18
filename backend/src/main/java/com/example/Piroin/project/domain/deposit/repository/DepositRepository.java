package com.example.Piroin.project.domain.deposit.repository;

import com.example.Piroin.project.domain.deposit.entity.Deposit;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Integer> {
    Optional<Deposit> findByUser(User user);

    Optional<Deposit> findByUserId(Long userId);

    List<Deposit> findByUserIdIn(List<Long> userIds);
}
