package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.Question;                  // ← StudySession 대신 Question으로 변경
import com.example.Piroin.project.domain.question.entity.QuestionAnonymousIdentity;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionAnonymousIdentityRepository extends JpaRepository<QuestionAnonymousIdentity, Long> {
    /*
    해당 질문에서 유저의 익명 번호 조회
    용도: 댓글 작성 시 이미 번호가 있는지 확인
    */
    Optional<QuestionAnonymousIdentity> findByQuestionAndUser(Question question, User user);

    /*
    해당 질문에 현재까지 부여된 익명 번호 수 조회
    용도: 새 익명 번호 발급 시(현재 수 + 1)
    */
    int countByQuestion(Question question);
}