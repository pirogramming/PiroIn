package com.example.Piroin.project.domain.curriculum.dto;

import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class CurriculumReqDTO {

    @Getter
    @NoArgsConstructor
    public static class CreateDayReq {
        @NotNull(message = "기수를 입력해주세요.")
        private Integer generation;

        @NotNull(message = "주차를 입력해주세요.")
        private Long week;

        @NotNull(message = "세션 날짜를 입력해주세요.")
        private LocalDate sessionDate;

        @NotEmpty(message = "세션 목록을 입력해주세요.")
        @Valid
        private List<SessionReq> sessions;
    }

    @Getter
    @NoArgsConstructor
    public static class SessionReq {
        @NotNull(message = "세션 시간대를 입력해주세요.")
        private SessionDayPart dayPart;

        @NotNull(message = "세션 제목을 입력해주세요.")
        private String title;

        @NotNull(message = "발표자를 입력해주세요.")
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
        @NotNull(message = "기수를 입력해주세요.")
        private Integer generation;

        @NotNull(message = "주차를 입력해주세요.")
        private Long week;

        private LocalDate newSessionDate;

        @NotEmpty(message = "세션 목록을 입력해주세요.")
        @Valid
        private List<UpdateSessionItemReq> sessions;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateSessionItemReq {
        @NotNull(message = "세션 시간대를 입력해주세요.")
        private SessionDayPart dayPart;

        private SessionStatus status;
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
