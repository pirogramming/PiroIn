package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.entity.QuestionAnonymousIdentity;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionAnonymousIdentityRepository extends JpaRepository<QuestionAnonymousIdentity, Long> {

    // 해당 질문에서 유저의 익명 번호 조회
    // 용도: 댓글 작성 시 이미 번호가 있는지 확인
    Optional<QuestionAnonymousIdentity> findByQuestionAndUser(Question question, User user);

    // 질문 상세 조회용: 댓글 작성자들의 익명 번호를 한 번에 조회
    @Query("""
            SELECT identity
            FROM QuestionAnonymousIdentity identity
            JOIN FETCH identity.user
            WHERE identity.question = :question
              AND identity.user.id IN :userIds
            """)
    List<QuestionAnonymousIdentity> findByQuestionAndUserIds(
            @Param("question") Question question,
            @Param("userIds") Set<Long> userIds
    );

    // 해당 질문에서 특정 역할(MEMBER/ADMIN)의 익명 번호 수 조회
    // 용도: 새 익명 번호 발급 시 역할별로 따로 카운트
    // MEMBER → 익명1, 익명2... / ADMIN → 운영진1, 운영진2...
    int countByQuestionAndUser_Role(Question question, Role role);

    @Query("SELECT COALESCE(MAX(a.anonymousNo), 0) FROM QuestionAnonymousIdentity a " + "WHERE a.question = :question AND a.user.role = :role")
    int findMaxAnonymousNoByQuestionAndRole(
            @Param("question") Question question,
            @Param("role") Role role
    );
}
