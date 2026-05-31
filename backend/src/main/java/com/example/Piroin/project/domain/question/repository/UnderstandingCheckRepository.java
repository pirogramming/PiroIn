package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.question.entity.UnderstandingCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnderstandingCheckRepository extends JpaRepository<UnderstandingCheck, Long> {
    /*
    특정 세션의 이해도 체크 목록(생성 최신순)
    용도: 세션 페이지에서 이해도 체크 목록 표시 시
    */
    List<UnderstandingCheck> findBySessionOrderByCreatedAtDesc(StudySession session);

    Page<UnderstandingCheck> findBySessionOrderByCreatedAtDesc(StudySession session, Pageable pageable);
}
