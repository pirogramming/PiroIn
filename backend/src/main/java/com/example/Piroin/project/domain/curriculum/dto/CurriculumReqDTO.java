package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class CurriculumReqDTO {

    @Getter
    @NoArgsConstructor
    public static class CreateDayReq {
        private Integer generation;
        private Long week;
        private LocalDate sessionDate;
        private List<SessionReq> sessions;
    }

    @Getter
    @NoArgsConstructor
    public static class SessionReq {
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private String description;
        private String sessionMaterialUrl;
        private String sessionMaterialName;
        private String recordingUrl;
        private String recordingPassword;
        // PM에만 사용
        private String assignmentUrl;
        private String assignmentName;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateSessionReq {
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
    }
}
