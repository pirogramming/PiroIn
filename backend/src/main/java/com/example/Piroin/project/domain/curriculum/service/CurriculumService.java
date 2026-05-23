package com.example.Piroin.project.domain.curriculum.service;

import com.example.Piroin.project.domain.curriculum.converter.CurriculumConverter;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import com.example.Piroin.project.domain.curriculum.exception.CurriculumException;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import com.example.Piroin.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CurriculumResDTO.CreateDayRes> getAllDays() {
        Map<LocalDate, List<StudySession>> grouped = curriculumRepository.findAllByOrderBySessionDateAscDayPartAsc()
                .stream()
                .collect(Collectors.groupingBy(StudySession::getSessionDate, Collectors.toList()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> CurriculumConverter.toCreateDayRes(entry.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CurriculumResDTO.CreateDayRes createDay(CurriculumReqDTO.CreateDayReq req) {
        if (req.getGeneration() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "기수는 필수입니다.");
        if (req.getWeek() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "주차는 필수입니다.");
        if (req.getSessionDate() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "세션 날짜는 필수입니다.");
        if (req.getSessions() == null || req.getSessions().size() != 2)
            throw new CurriculumException(HttpStatus.BAD_REQUEST, "AM/PM 세션 2개를 함께 입력해야 합니다.");

        req.getSessions().forEach(s -> {
            if (s.getDayPart() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "dayPart는 필수입니다.");
            if (s.getTitle() == null || s.getTitle().isBlank()) throw new CurriculumException(HttpStatus.BAD_REQUEST, "세션 제목은 필수입니다.");
        });

        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new CurriculumException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<StudySession> sessions = req.getSessions().stream()
                .map(sessionReq -> CurriculumConverter.toStudySession(sessionReq, req, user))
                .collect(Collectors.toList());

        return CurriculumConverter.toCreateDayRes(curriculumRepository.saveAll(sessions));
    }

    @Transactional
    public CurriculumResDTO.CreateDayRes updateDay(LocalDate sessionDate, CurriculumReqDTO.UpdateDayReq req) {
        List<StudySession> sessions = curriculumRepository.findBySessionDate(sessionDate);
        if (sessions.isEmpty()) throw new CurriculumException(HttpStatus.NOT_FOUND, "해당 세션을 찾을 수 없습니다.");

        req.getSessions().forEach(sessionReq -> {
            if (sessionReq.getTitle() == null || sessionReq.getTitle().isBlank())
                throw new CurriculumException(HttpStatus.BAD_REQUEST, "세션 제목은 필수입니다.");

            sessions.stream()
                    .filter(s -> s.getDayPart() == sessionReq.getDayPart())
                    .findFirst()
                    .ifPresent(s -> s.updateFull(
                            req.getGeneration(), req.getWeek(),
                            sessionReq.getTitle(), sessionReq.getHostName(),
                            sessionReq.getSessionMaterialUrl(),
                            sessionReq.getSessionMaterialName(), sessionReq.getRecordingUrl(),
                            sessionReq.getRecordingPassword(), sessionReq.getAssignmentUrl(),
                            sessionReq.getAssignmentName()
                    ));
        });

        return CurriculumConverter.toCreateDayRes(sessions);
    }

    @Transactional
    public void deleteDay(LocalDate sessionDate) {
        List<StudySession> sessions = curriculumRepository.findBySessionDate(sessionDate);
        if (sessions.isEmpty()) throw new CurriculumException(HttpStatus.NOT_FOUND, "해당 세션을 찾을 수 없습니다.");
        curriculumRepository.deleteAll(sessions);
    }

    @Transactional(readOnly = true)
    public CurriculumResDTO.QnaSessionsResponse getQnaSessions() {
        List<CurriculumResDTO.ActiveSessionResponse> activeSessions = curriculumRepository
                .findByStatusOrderBySessionDateAscDayPartAsc(SessionStatus.IN_SESSION)
                .stream()
                .map(this::toActiveSessionResponse)
                .toList();

        List<CurriculumResDTO.PastSessionResponse> pastSessions = curriculumRepository
                .findByStatusOrderBySessionDateDescDayPartDesc(SessionStatus.AFTER_SESSION)
                .stream()
                .map(this::toPastSessionResponse)
                .toList();

        return new CurriculumResDTO.QnaSessionsResponse(activeSessions, pastSessions);
    }

    private CurriculumResDTO.ActiveSessionResponse toActiveSessionResponse(StudySession session) {
        return new CurriculumResDTO.ActiveSessionResponse(
                session.getId(),
                session.getWeek().intValue(),
                session.getSessionDate().getDayOfWeek().name(),
                session.getDayPart().name(),
                session.getSessionDate().toString(),
                session.getTitle()
        );
    }

    private CurriculumResDTO.PastSessionResponse toPastSessionResponse(StudySession session) {
        return new CurriculumResDTO.PastSessionResponse(
                session.getId(),
                session.getWeek().intValue(),
                session.getSessionDate().getDayOfWeek().name(),
                session.getDayPart().name(),
                session.getTitle()
        );
    }
}
