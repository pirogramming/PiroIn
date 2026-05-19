package com.example.Piroin.project.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // SERIAL 타입에 매칭 (Long -> Integer)

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "attendance_order")
    private String attendanceOrder; // '1, 2, 3' 코멘트 항목

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired; // BOOLEAN 타입에 매칭

    @Column(name = "field3")
    private String field3;

    public void expire() {
        this.isExpired = true;
    }
}