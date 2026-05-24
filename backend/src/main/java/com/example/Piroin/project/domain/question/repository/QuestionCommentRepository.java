package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.entity.QuestionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionCommentRepository extends JpaRepository<QuestionComment, Long> {
    /*
    특정 질문의 삭제되지 않은 최상위 댓글 목록(등록순)
    parentComment가 null인 것 = 대댓글이 아닌 최상위 댓글
    용도: 질문 상세 페이지에서 댓글 목록 표시 시
    */
    List<QuestionComment> findByQuestionAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(Question question);

    /*
    질문 목록 미리보기용 최신 최상위 댓글 3개
    조회는 최신순으로 가져오고, 서비스에서 오래된 순으로 다시 정렬해 내려준다.
    */
    List<QuestionComment> findTop3ByQuestionAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(Question question);

    /*
    특정 댓글의 대댓글 목록(등록순)
    용도: 댓글 아래 대댓글을 가져올 때
    */
    List<QuestionComment> findByParentCommentAndDeletedAtIsNullOrderByCreatedAtAsc(QuestionComment parentComment);

    /*
    특정 질문의 삭제되지 않은 댓글 수(대댓글 포함)
    용도: 질문 목록에서 "댓글 N개" 표시 시
    */
    int countByQuestionAndDeletedAtIsNull(Question question);
}
