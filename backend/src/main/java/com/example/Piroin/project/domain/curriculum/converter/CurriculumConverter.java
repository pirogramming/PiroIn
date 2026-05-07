package com.example.Piroin.project.domain.curriculum.converter;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import com.example.Piroin.project.domain.user.entity.User;

import java.time.LocalDateTime;

public class CurriculumConverter {

    public static StudySession toStudySession(CurriculumReqDTO.CreateSessionReq req, User user) {
        return StudySession.builder()
                .createdBy(user)
                .generation(req.getGeneration())
                .week(req.getWeek())
                .sessionDate(req.getSessionDate())
                .dayPart(req.getDayPart())
                .title(req.getTitle())
                .hostName(req.getHostName())
                .status(SessionStatus.BEFORE_SESSION)
                .description(req.getDescription())
                .sessionMaterialUrl(req.getSessionMaterialUrl())
                .assignmentUrl(req.getAssignmentUrl())
                .recordingUrl(req.getRecordingUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CurriculumResDTO.CreateSessionRes toCreateSessionRes(StudySession session) {
        return new CurriculumResDTO.CreateSessionRes(
                session.getId(),
                session.getGeneration(),
                session.getWeek(),
                session.getSessionDate(),
                session.getDayPart(),
                session.getTitle(),
                session.getHostName(),
                session.getStatus(),
                session.getDescription(),
                session.getSessionMaterialUrl(),
                session.getAssignmentUrl(),
                session.getRecordingUrl(),
                session.getCreatedAt()
        );
    }

    public static CurriculumResDTO.UpdateSessionRes toUpdateSessionRes(StudySession session) {
        return new CurriculumResDTO.UpdateSessionRes(
                session.getId(),
                session.getGeneration(),
                session.getWeek(),
                session.getSessionDate(),
                session.getDayPart(),
                session.getTitle(),
                session.getHostName(),
                session.getStatus(),
                session.getDescription(),
                session.getSessionMaterialUrl(),
                session.getAssignmentUrl(),
                session.getRecordingUrl(),
                session.getUpdatedAt()
        );
    }
}
