package com.example.Piroin.project.domain.question.entity;

import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
    대댓글을 위한 부모 댓글 참조
    null이면 → 일반 댓글(최상위)
    값이 있으면 → 대댓글(parentComment가 부모)
    같은 QuestionComment 테이블을 자기 자신이 참조하는 구조
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private QuestionComment parentComment;

    @Column(columnDefinition = "TEXT")
    private String content;

    /*
    이미지 URL 목록을 JSON 배열 문자열로 저장
    Question 엔티티와 동일한 방식 사용
    */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 이미지 URL 목록 조회
    @Transient
    public List<String> getImageUrls() {
        return Question.parseImageUrls(this.imageUrl);
    }

    // 댓글 내용 수정
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // 댓글 소프트 삭제
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}