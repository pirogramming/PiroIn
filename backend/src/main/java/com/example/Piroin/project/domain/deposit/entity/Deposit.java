package com.example.Piroin.project.domain.deposit.entity;

import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "deposit",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_deposit_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "descent_assignment", nullable = false)
    private Integer descentAssignment;

    @Column(name = "descent_attendance", nullable = false)
    private Integer descentAttendance;

    @Column(name = "ascent_defence", nullable = false)
    private Integer ascentDefence;
}

