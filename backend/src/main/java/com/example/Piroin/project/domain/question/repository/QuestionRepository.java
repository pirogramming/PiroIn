package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.question.entity.Question;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /*
    질문 상세 조회용: 질문 작성자를 함께 가져와 상세 DTO 조립 중 추가 조회를 피한다.
    */
    @Query("""
            SELECT question
            FROM Question question
            JOIN FETCH question.user
            WHERE question.id = :id
              AND question.deletedAt IS NULL
            """)
    Optional<Question> findDetailByIdAndDeletedAtIsNull(@Param("id") Long id);

    /*
    좋아요 카운트 갱신용: 같은 질문에 대한 동시 토글 요청을 직렬화해 likeCount lost update를 방지한다.
    */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT question
            FROM Question question
            WHERE question.id = :id
              AND question.deletedAt IS NULL
            """)
    Optional<Question> findByIdAndDeletedAtIsNullForUpdate(@Param("id") Long id);
}
