package com.example.Piroin.project.domain.attendance.repository;

import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findById(Integer id);


    // 연관관계 필드명이 attendanceCode 라면 내부 ID인 Id를 조합하여 명명
    Optional<Attendance> findByUserIdAndAttendanceCodeId(Long userId, Long attendanceCodeId);

    //List<Attendance> findByUserIdAndStudySessionSessionDate(Integer userId, LocalDate date);

    int countByUserAndStatusFalse(User user);

    // 1. 특정 출석 코드 ID에 해당하는 결석 데이터 조회
    List<Attendance> findByAttendanceCodeIdAndStatusFalse(Integer attendanceCodeId);

    @Query("""
            SELECT COUNT(a)
            FROM Attendance a
            WHERE a.attendanceCode.attendanceDate = :attendanceDate
              AND a.attendanceCode.attendanceOrder = :attendanceOrder
              AND a.status = true
            """)
    long countAttendedByDateAndOrder(
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("attendanceOrder") String attendanceOrder
    );

    // 2. 특정 유저 ID와 출석 코드의 날짜 조건으로 조회 (엔티티 그래프 참조: attendanceCode.attendanceDate)
    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.attendanceCode.attendanceDate = :attendanceDate")
    List<Attendance> findByUserIdAndDate(@Param("userId") Integer userId, @Param("attendanceDate") LocalDate attendanceDate);

    // 3. 특정 유저의 모든 출석 데이터 조회
    List<Attendance> findByUserId(Long userId);

    Optional<Attendance> findByUserIdAndAttendanceCodeId(
            Long userId,
            Integer attendanceCodeId
    );

    List<Attendance> findByAttendanceCodeId(Integer id);

    // 특정 날짜에 발급된 출석 코드의 개수를 세는 메서드
    //long countByAttendanceDate(String attendanceDate);

    // 현재 만료되지 않은(활성화된) 출석 코드 목록을 가져오는 메서드
    //List<AttendanceCode> findByIsExpiredFalse();
}

