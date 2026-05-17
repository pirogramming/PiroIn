package com.example.Piroin.project.domain.attendance.repository;

import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface AttendanceCodeRepository extends JpaRepository<AttendanceCode, Long> {

    @Query("""
        select count(ac)
        from AttendanceCode ac
        where ac.studySession.sessionDate = :date
    """)
    int countByStudySessionDate(@Param("date") LocalDate date);


    // [추가] 모든 활성화된 코드를 한 번에 만료 처리 (벌크 연산)
    @Modifying
    @Query("update AttendanceCode ac set ac.isExpired = true where ac.isExpired = false")
    void expireAllActiveCodes();

    Optional<AttendanceCode> findFirstByIsExpiredFalseOrderByIdDesc();

    List<AttendanceCode> findByStudySessionId(Long studySessionId);

    Optional<AttendanceCode> findByCodeAndStudySessionId(String code, Long studySessionId);

    // 특정 날짜에 발급된 코드 개수 조회
    long countByAttendanceDate(String attendanceDate);

    // 만료되지 않은 코드 목록 조회
    List<AttendanceCode> findByIsExpiredFalse();
}


