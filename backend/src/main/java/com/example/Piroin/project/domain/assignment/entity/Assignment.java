package com.example.Piroin.project.domain.assignment.entity;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private StudySession session;

    @Column(nullable = false)
    private String title;

    private String content;
}

