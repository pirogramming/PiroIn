package com.example.Piroin.project.domain.question.entity;

import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "question_anonymous_identity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_question_anonymous_identity_question_user",
                        columnNames = {"question_id", "user_id"}
                ),
                @UniqueConstraint(
                        name = "uq_question_anonymous_identity_question_role_no",
                        columnNames = {"question_id", "role", "anonymous_no"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionAnonymousIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "anonymous_no", nullable = false)
    private Integer anonymousNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
