package com.example.Piroin.project.domain.curriculum.entity;

import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;

@Entity
@Table(name = "study_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Integer generation;

    @Column(nullable = false)
    private Long week;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionDayPart dayPart;

    @Column(nullable = false)
    private String title;

    private String hostName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String sessionMaterialUrl;

    @Column(columnDefinition = "TEXT")
    private String assignmentUrl;

    @Column(columnDefinition = "TEXT")
    private String recordingUrl;


    // 녹화본 비밀번호 여기에 추가했습니당. sql 파일에도 물론 반영했고요.
    @Column(length = 60)
    private String recordingPassword;


    // 세션 자료 이름, 과제 자료 이름 추가.
    @Column(length = 255)
    private String sessionMaterialName;

    @Column(length = 255)
    private String assignmentName;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateFull(Integer generation, Long week, LocalDate sessionDate, SessionStatus status, String title, String hostName,
                           String sessionMaterialUrl, String sessionMaterialName,
                           String recordingUrl, String recordingPassword,
                           String assignmentUrl, String assignmentName) {
        if (generation != null) this.generation = generation;
        if (week != null) this.week = week;
        if (sessionDate != null) this.sessionDate = sessionDate;
        if (status != null) this.status = status;
        this.title = title;
        this.hostName = (hostName != null && !hostName.isBlank()) ? hostName : "(미정)";
        this.sessionMaterialUrl = sessionMaterialUrl;
        this.sessionMaterialName = sessionMaterialName;
        this.recordingUrl = recordingUrl;
        this.recordingPassword = recordingPassword;
        this.assignmentUrl = assignmentUrl;
        this.assignmentName = assignmentName;
        this.updatedAt = LocalDateTime.now();
    }

}

