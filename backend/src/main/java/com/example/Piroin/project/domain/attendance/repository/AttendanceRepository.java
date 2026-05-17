package com.example.Piroin.project.domain.attendance.repository;

import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // List<Attendance> findByUserId(Long userId);

    Optional<Attendance> findByUserIdAndStudySessionId(Long userId, Long studySessionId);

    boolean existsByUserIdAndStudySessionId(Long userId, Long studySessionId);

    List<Attendance> findByStudySessionId(Long studySessionId);

    List<Attendance> findByStudySessionIdAndStatusFalse(Long studySessionId);

    List<Attendance> findByUserIdAndStudySessionSessionDate(Long userId, LocalDate date);

    int countByUserAndStatusFalse(User user);

    // 1. 특정 출석 코드 ID에 해당하는 결석 데이터 조회
    List<Attendance> findByAttendanceCodeIdAndStatusFalse(Integer attendanceCodeId);

    // 2. 특정 유저 ID와 출석 코드의 날짜 조건으로 조회 (엔티티 그래프 참조: attendanceCode.attendanceDate)
    List<Attendance> findByUserIdAndAttendanceCodeAttendanceDate(Integer userId, String attendanceDate);

    // 3. 특정 유저의 모든 출석 데이터 조회
    List<Attendance> findByUserId(Integer userId);
}


