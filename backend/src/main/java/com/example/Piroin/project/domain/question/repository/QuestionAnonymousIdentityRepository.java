package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.question.entity.QuestionAnonymousIdentity;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionAnonymousIdentityRepository extends JpaRepository<QuestionAnonymousIdentity, Long> {
    /*
    한 세션 내에서 유저의 익명 ID 조회
    익명 번호는 질문 단위가 아닌 세션 단위로 부여
    (에브리타임처럼 같은 세션에서는 항상 같은 익명 번호 유지)
    용도: 질문/댓글 작성 시 이미 번호가 있는지 확인
    */
    Optional<QuestionAnonymousIdentity> findBySessionAndUser(StudySession session, User user);

    /*
    한 세션에서 현재까지 부여된 익명 번호의 최댓값 조회
    용도: 새 익명 번호 발급 시(최댓값 + 1)
    */
    int countBySession(StudySession session);
}