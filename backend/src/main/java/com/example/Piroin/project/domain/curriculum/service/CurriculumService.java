package com.example.Piroin.project.domain.curriculum.service;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.enums.SessionStatus;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurriculumService {
    private final CurriculumRepository curriculumRepository;

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
