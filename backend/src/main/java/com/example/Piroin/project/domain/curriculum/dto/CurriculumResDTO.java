package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CurriculumResDTO {

    @Getter
    @AllArgsConstructor
    public static class CreateSessionRes {
        private Long id;
        private String createdBy;
        private Integer generation;
        private Long week;
        private LocalDate sessionDate;
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private SessionStatus status;
        private String description;
        private String sessionMaterialUrl;
        private String assignmentUrl;
        private String recordingUrl;
        private String recordingPassword;
        private String sessionMaterialName;
        private String assignmentName;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    public static class GetSessionRes {
        private Long id;
        private Long week;
        private LocalDate sessionDate;
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private SessionStatus status;
        private String description;
        private String sessionMaterialUrl;
        private String assignmentUrl;
        private String recordingUrl;
        private String recordingPassword;
        private String sessionMaterialName;
        private String assignmentName;
    }

    @Getter
    @AllArgsConstructor
    public static class UpdateSessionRes {
        private Long id;
        private String createdBy;
        private Integer generation;
        private Long week;
        private LocalDate sessionDate;
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private SessionStatus status;
        private String description;
        private String sessionMaterialUrl;
        private String assignmentUrl;
        private String recordingUrl;
        private String recordingPassword;
        private String sessionMaterialName;
        private String assignmentName;
        private LocalDateTime updatedAt;
    }

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
