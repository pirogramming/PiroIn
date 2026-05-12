package com.example.Piroin.project.domain.assignment.entity;

import com.example.Piroin.project.domain.assignment.enums.AssignmentStatus;
import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "assignment_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_assignment_item_user_assignment",
                        columnNames = {"user_id", "assignment_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AssignmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus submitted;

    public void updateSubmitted(AssignmentStatus submitted) {
        this.submitted = submitted;
    }

}
