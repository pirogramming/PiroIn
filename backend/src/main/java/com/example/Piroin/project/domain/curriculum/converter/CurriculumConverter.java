package com.example.Piroin.project.domain.curriculum.converter;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.entity.WeeklyMvp;
import com.example.Piroin.project.domain.curriculum.enums.SessionDayPart;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import com.example.Piroin.project.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CurriculumConverter {

    public static StudySession toStudySession(CurriculumReqDTO.SessionReq sessionReq,
                                              CurriculumReqDTO.CreateDayReq req,
                                              User user) {
        return StudySession.builder()
                .createdBy(user)
                .generation(req.getGeneration())
                .week(req.getWeek())
                .sessionDate(req.getSessionDate())
                .dayPart(sessionReq.getDayPart())
                .title(sessionReq.getTitle())
                .hostName(sessionReq.getHostName() != null ? sessionReq.getHostName() : "(미정)")
                .status(SessionStatus.BEFORE_SESSION)
                .sessionMaterialUrl(sessionReq.getSessionMaterialUrl())
                .sessionMaterialName(sessionReq.getSessionMaterialName())
                .recordingUrl(sessionReq.getRecordingUrl())
                .recordingPassword(sessionReq.getRecordingPassword())
                .assignmentUrl(sessionReq.getAssignmentUrl())
                .assignmentName(sessionReq.getAssignmentName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CurriculumResDTO.CreateDayRes toCreateDayRes(List<StudySession> sessions) {
        StudySession first = sessions.get(0);

        // assignment는 PM 세션 기준으로 저장/조회. 운영 기간 6주 고정
        StudySession pm = sessions.stream()
                .filter(s -> s.getDayPart() == SessionDayPart.PM)
                .findFirst()
                .orElse(null);

        return new CurriculumResDTO.CreateDayRes(
                first.getSessionDate(),
                first.getGeneration(),
                first.getWeek(),
                pm != null ? pm.getAssignmentUrl() : null,
                pm != null ? pm.getAssignmentName() : null,
                sessions.stream().map(CurriculumConverter::toSessionInfo).collect(Collectors.toList()),
                first.getCreatedAt()
        );
    }

    public static CurriculumResDTO.SessionInfo toSessionInfo(StudySession session) {
        return new CurriculumResDTO.SessionInfo(
                session.getId(),
                session.getDayPart(),
                session.getTitle(),
                session.getHostName(),
                session.getStatus(),
                session.getSessionMaterialUrl(),
                session.getSessionMaterialName(),
                session.getRecordingUrl(),
                session.getRecordingPassword()
        );
    }

    public static CurriculumResDTO.MvpRes toMvpRes(WeeklyMvp mvp) {
        return new CurriculumResDTO.MvpRes(
                mvp.getWeek1Mvp(),
                mvp.getWeek2Mvp(),
                mvp.getWeek3Mvp(),
                mvp.getWeek4Mvp(),
                mvp.getWeek5Mvp(),
                mvp.getChallengeMvp()
        );
    }

}