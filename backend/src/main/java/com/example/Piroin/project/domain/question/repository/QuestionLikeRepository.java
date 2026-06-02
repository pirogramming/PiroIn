package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.entity.QuestionLike;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long> {
    /*
    특정 유저가 특정 질문에 좋아요를 눌렀는지 조회
    용도: 좋아요 토글 시 이미 눌렀는지 확인
    */
    Optional<QuestionLike> findByQuestionAndUser(Question question, User user);

    /*
    특정 유저가 특정 질문에 좋아요를 눌렀는지 여부(boolean)
    용도: 질문 상세 응답에 is_liked 필드 포함 시
    */
    boolean existsByQuestionAndUser(Question question, User user);

    // 특정 유저가 좋아요를 누른 질문 ID 목록을 한 번에 조회
    // 용도: 질문 목록 조회 시 N+1 방지
    @Query("SELECT ql.question.id FROM QuestionLike ql WHERE ql.question.id IN :questionIds AND ql.user = :user")
    List<Long> findLikedQuestionIdsByQuestionIdsAndUser(
            @Param("questionIds") List<Long> questionIds,
            @Param("user") User user
    );
}