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
    private Integer id;

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

    // 출석 차감액만 새로 계산할 때 사용
    public void updateAttendanceAmount(Integer descentAttendance) {
        this.descentAttendance = descentAttendance;

        int baseAmount = 100_000;

        this.amount = baseAmount
                - this.descentAssignment
                - this.descentAttendance
                + this.ascentDefence;
    }

    // 과제 차감 + 출석 차감을 한 번에 재계산할 때 사용
    public void updateDepositAmount(
            Integer descentAssignment,
            Integer descentAttendance
    ) {
        this.descentAssignment = descentAssignment;
        this.descentAttendance = descentAttendance;

        int baseAmount = 100_000;

        this.amount = baseAmount
                - this.descentAssignment
                - this.descentAttendance
                + this.ascentDefence;
    }


}

