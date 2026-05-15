package com.example.Piroin.project.domain.question.entity;

import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "understanding_response",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_understanding_response_check_user",
                        columnNames = {"check_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UnderstandingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_id", nullable = false)
    private UnderstandingCheck check;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnderstandResChoice choice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasChoice(UnderstandResChoice choice) {
        return this.choice == choice;
    }

    public void changeChoice(UnderstandResChoice choice) {
        this.choice = choice;
        this.updatedAt = LocalDateTime.now();
    }
}
