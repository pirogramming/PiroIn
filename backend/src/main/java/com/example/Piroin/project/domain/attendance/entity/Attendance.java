package com.example.Piroin.project.domain.attendance.entity;

import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import javax.xml.crypto.dsig.Manifest;

@Entity
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // SERIAL 타입에 매칭 (Long -> Integer)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_code_id", nullable = false)
    private AttendanceCode attendanceCode; // attendance_code_id 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // user_id 매핑

    @Column(nullable = false)
    private Boolean status; // BOOLEAN 타입에 매칭

    public void updateStatus(Boolean status) {
        this.status = status;
    }


}