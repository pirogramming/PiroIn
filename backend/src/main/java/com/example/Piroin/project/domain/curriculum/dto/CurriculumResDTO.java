package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CurriculumResDTO {

    public record SessionInfo(
            Long sessionId,
            SessionDayPart dayPart,
            String title,
            String hostName,
            SessionStatus status,
            String sessionMaterialUrl,
            String sessionMaterialName,
            String recordingUrl,
            String recordingPassword
    ) {}

    public record CreateDayRes(
            LocalDate sessionDate,
            Integer generation,
            Long week,
            String assignmentUrl,
            String assignmentName,
            List<SessionInfo> sessions,
            LocalDateTime createdAt
    ) {}

    public record QnaSessionsResponse(
            List<ActiveSessionResponse> activeSessions,
            List<PastSessionResponse> pastSessions
    ) {
    }

    public record ActiveSessionResponse(
            Long sessionId,
            Integer week,
            String dayOfWeek,
            String dayPart,
            String sessionDate,
            String title
    ) {
    }

    public record PastSessionResponse(
            Long sessionId,
            Integer week,
            String dayOfWeek,
            String dayPart,
            String title
    ) {
    }

    // 과제 MVP 명예의 전당
    // 값이 없는 주차는 null로 내려가고, 프론트에서 null인 항목은 숨김
    public record MvpRes(
            String week1Mvp,
            String week2Mvp,
            String week3Mvp,
            String week4Mvp,
            String week5Mvp,
            String challengeMvp
    ) {
    }
}