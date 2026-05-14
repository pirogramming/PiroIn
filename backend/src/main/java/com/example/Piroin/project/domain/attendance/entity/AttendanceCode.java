package com.example.Piroin.project.domain.attendance.entity;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id")
    private StudySession studySession;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;

    public void expire() {
        this.isExpired = true;
    }
}

