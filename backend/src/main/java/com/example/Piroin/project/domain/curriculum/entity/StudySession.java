package com.example.Piroin.project.domain.curriculum.entity;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
