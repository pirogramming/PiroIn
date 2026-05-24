package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.entity.QuestionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionCommentRepository extends JpaRepository<QuestionComment, Long> {
    /*
    특정 질문의 삭제되지 않은 최상위 댓글 목록(등록순)
    parentComment가 null인 것 = 대댓글이 아닌 최상위 댓글
    용도: 질문 상세 페이지에서 댓글 목록 표시 시
    */
    List<QuestionComment> findByQuestionAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(Question question);

    /*
    질문 목록 미리보기용 최상위 댓글 3개를 질문별로 한 번에 조회한다.
    row_number()로 각 질문의 오래된 댓글 3개만 남겨 N+1 조회를 피한다.
    */
    @Query(value = """
            SELECT ranked.question_id AS "questionId",
                   ranked.id AS "commentId",
                   ranked.user_id AS "userId",
                   u.role AS "userRole",
                   ranked.content AS "content",
                   ranked.created_at AS "createdAt",
                   qai.anonymous_no AS "anonymousNo"
            FROM (
                SELECT qc.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY qc.question_id
                           ORDER BY qc.created_at ASC, qc.id ASC
                       ) AS rn
                FROM question_comment qc
                WHERE qc.question_id IN (:questionIds)
                  AND qc.parent_comment_id IS NULL
                  AND qc.deleted_at IS NULL
            ) ranked
            JOIN users u ON u.id = ranked.user_id
            LEFT JOIN question_anonymous_identity qai
                   ON qai.question_id = ranked.question_id
                  AND qai.user_id = ranked.user_id
            WHERE ranked.rn <= 3
            ORDER BY ranked.question_id ASC, ranked.created_at ASC, ranked.id ASC
            """, nativeQuery = true)
    List<PreviewCommentRow> findPreviewCommentsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /*
    질문 목록의 댓글 수를 질문별로 한 번에 조회한다.
    대댓글도 댓글 수에 포함한다.
    */
    @Query("""
            SELECT comment.question.id AS questionId,
                   COUNT(comment.id) AS commentCount
            FROM QuestionComment comment
            WHERE comment.question.id IN :questionIds
              AND comment.deletedAt IS NULL
            GROUP BY comment.question.id
            """)
    List<CommentCountRow> countByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /*
    특정 댓글의 대댓글 목록(등록순)
    용도: 댓글 아래 대댓글을 가져올 때
    */
    List<QuestionComment> findByParentCommentAndDeletedAtIsNullOrderByCreatedAtAsc(QuestionComment parentComment);

    interface PreviewCommentRow {
        Long getQuestionId();

        Long getCommentId();

        Long getUserId();

        String getUserRole();

        String getContent();

        LocalDateTime getCreatedAt();

        Integer getAnonymousNo();
    }

    interface CommentCountRow {
        Long getQuestionId();

        Long getCommentCount();
    }
}
