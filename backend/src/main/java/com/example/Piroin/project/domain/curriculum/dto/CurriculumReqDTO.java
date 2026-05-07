package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CurriculumReqDTO {

    @Getter
    @NoArgsConstructor
    public static class CreateSessionReq {
        private Long userId;
        private Integer generation;
        private Long week;
        private LocalDate sessionDate;
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private String description;
        private String sessionMaterialUrl;
        private String assignmentUrl;
        private String recordingUrl;
    }
}
