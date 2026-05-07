package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CurriculumResDTO {

    @Getter
    @AllArgsConstructor
    public static class CreateSessionRes {
        private Long id;
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
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    public static class UpdateSessionRes {
        private Long id;
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
        private LocalDateTime updatedAt;
    }
}
