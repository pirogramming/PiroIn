package com.example.Piroin.project.domain.deposit.service;

import com.example.Piroin.project.domain.attendance.repository.AttendanceRepository;
import com.example.Piroin.project.domain.deposit.entity.Deposit;
import com.example.Piroin.project.domain.deposit.repository.DepositRepository;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositService {

    private static final int ATTENDANCE_PENALTY = 10_000;

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public void recalculateDeposit(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Deposit deposit = depositRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("보증금 정보가 존재하지 않습니다."));

        int failAttendanceCount = attendanceRepository.countByUserAndStatusFalse(user);
        int descentAttendance = failAttendanceCount * ATTENDANCE_PENALTY;

        deposit.updateAttendanceAmount(descentAttendance);
    }
}
