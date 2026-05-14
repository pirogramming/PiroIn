package com.example.Piroin.project.domain.attendance.repository;

import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserId(Long userId);

    Optional<Attendance> findByUserIdAndStudySessionId(Long userId, Long studySessionId);

    boolean existsByUserIdAndStudySessionId(Long userId, Long studySessionId);

    List<Attendance> findByStudySessionId(Long studySessionId);

    List<Attendance> findByStudySessionIdAndStatusFalse(Long studySessionId);

    List<Attendance> findByUserIdAndStudySessionSessionDate(Long userId, LocalDate date);

    int countByUserAndStatusFalse(User user);
}


