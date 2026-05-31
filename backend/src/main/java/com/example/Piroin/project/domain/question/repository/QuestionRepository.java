package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    /*
    특정 세션의 삭제되지 않은 질문 목록 조회
    용도: 질문 목록 API에서 세션별 질문을 가져올 때
    */
    List<Question> findBySessionAndDeletedAtIsNull(StudySession session);

    /*
    ID로 삭제되지 않은 질문 단건 조회
    용도: 질문 상세 조회, 수정, 삭제, 좋아요 처리 시
    */
    Optional<Question> findByIdAndDeletedAtIsNull(Long id);
}