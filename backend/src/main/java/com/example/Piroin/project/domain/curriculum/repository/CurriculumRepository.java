package com.example.Piroin.project.domain.curriculum.repository;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
StudySession(세션) DB 접근 인터페이스
Q&A 서비스에서 세션 존재 여부 확인 시 사용
JpaRepository<엔티티 타입, PK 타입> 을 상속하면 findById, save, delete 등 기본 메서드가 자동으로 제공
*/
public interface CurriculumRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByStatusOrderBySessionDateAscDayPartAsc(SessionStatus status);
}
