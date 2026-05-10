package com.example.Piroin.project.domain.attendance.repository;

import com.example.Piroin.project.domain.attendance.entity.AttendanceCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// AttendanceCodeRepository 코드는 아직 수정하지 않음.
public interface AttendanceCodeRepository extends JpaRepository<AttendanceCode, Long> {

    @Query("""
        select count(ac)
        from AttendanceCode ac
        where ac.studySession.session_date = :date
    """)
    int countByStudySessionDate(@Param("date") LocalDate date);

    List<AttendanceCode> findByIsExpiredFalse();

    Optional<AttendanceCode> findFirstByIsExpiredFalseOrderByIdDesc();

    Optional<AttendanceCode> findByCodeAndIsExpiredFalse(String code);

    List<AttendanceCode> findByStudySessionId(Long studySessionId);
}


