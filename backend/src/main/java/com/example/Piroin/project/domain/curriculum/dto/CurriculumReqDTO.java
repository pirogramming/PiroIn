package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
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
    public static class UpdateDayReq {
        private Integer generation;
        private Long week;
        private List<UpdateSessionItemReq> sessions;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateSessionItemReq {
        private SessionDayPart dayPart;
        private String title;
        private String hostName;
        private String sessionMaterialUrl;
        private String sessionMaterialName;
        private String recordingUrl;
        private String recordingPassword;
        private String assignmentUrl;
        private String assignmentName;
    }

}
