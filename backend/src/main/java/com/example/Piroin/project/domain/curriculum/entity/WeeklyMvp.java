package com.example.Piroin.project.domain.curriculum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
과제 MVP 명예의 전당 데이터
운영진 공지용으로만 쓰이는 단일 row 테이블이라 PK를 고정값(1L)으로 사용
*/
@Entity
@Table(name = "weekly_mvp")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyMvp {

    @Id
    private Long id;

    @Column(name = "week1_mvp", length = 100)
    private String week1Mvp;

    @Column(name = "week2_mvp", length = 100)
    private String week2Mvp;

    @Column(name = "week3_mvp", length = 100)
    private String week3Mvp;

    @Column(name = "week4_mvp", length = 100)
    private String week4Mvp;

    @Column(name = "week5_mvp", length = 100)
    private String week5Mvp;

    @Column(name = "challenge_mvp", length = 100)
    private String challengeMvp;

    private LocalDateTime updatedAt;

    public void update(String week1Mvp, String week2Mvp, String week3Mvp,
                        String week4Mvp, String week5Mvp, String challengeMvp) {
        this.week1Mvp = normalize(week1Mvp);
        this.week2Mvp = normalize(week2Mvp);
        this.week3Mvp = normalize(week3Mvp);
        this.week4Mvp = normalize(week4Mvp);
        this.week5Mvp = normalize(week5Mvp);
        this.challengeMvp = normalize(challengeMvp);
        this.updatedAt = LocalDateTime.now();
    }

    // 빈 문자열은 '아직 미입력'으로 취급해서 null로 저장 (프론트에서 해당 주차를 숨기는 기준이 됨)
    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}