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
            String description,
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
}
