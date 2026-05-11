package com.example.Piroin.project.domain.curriculum.dto;

import java.util.List;

public class CurriculumResDTO {
    private Long id;
    private String title;

    public CurriculumResDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
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
