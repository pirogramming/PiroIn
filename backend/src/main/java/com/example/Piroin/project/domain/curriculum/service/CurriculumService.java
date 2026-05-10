package com.example.Piroin.project.domain.curriculum.service;

import com.example.Piroin.project.domain.curriculum.converter.CurriculumConverter;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import java.util.List;
import java.util.stream.Collectors;
import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.exception.CurriculumException;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CurriculumResDTO.GetSessionRes> getAllSessions() {
        return curriculumRepository.findAll().stream()
                .map(CurriculumConverter::toGetSessionRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public CurriculumResDTO.CreateSessionRes createSession(CurriculumReqDTO.CreateSessionReq req) {
        if (req.getGeneration() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "기수는 필수입니다.");
        if (req.getWeek() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "주차는 필수입니다.");
        if (req.getSessionDate() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "세션 날짜는 필수입니다.");
        if (req.getDayPart() == null) throw new CurriculumException(HttpStatus.BAD_REQUEST, "오전/오후는 필수입니다.");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new CurriculumException(HttpStatus.BAD_REQUEST, "제목은 필수입니다.");

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new CurriculumException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        StudySession session = CurriculumConverter.toStudySession(req, user);
        StudySession savedSession = curriculumRepository.save(session);

        return CurriculumConverter.toCreateSessionRes(savedSession);
    }

    @Transactional
    public CurriculumResDTO.UpdateSessionRes updateSession(Long sessionId, CurriculumReqDTO.UpdateSessionReq req) {
        StudySession session = curriculumRepository.findById(sessionId)
                .orElseThrow(() -> new CurriculumException(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));

        session.update(req.getGeneration(), req.getWeek(), req.getSessionDate(), req.getDayPart(),
                req.getTitle(), req.getHostName(), req.getStatus(), req.getDescription(),
                req.getSessionMaterialUrl(), req.getAssignmentUrl(), req.getRecordingUrl(),
                req.getRecordingPassword(), req.getSessionMaterialName(), req.getAssignmentName());

        return CurriculumConverter.toUpdateSessionRes(session);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        StudySession session = curriculumRepository.findById(sessionId)
                .orElseThrow(() -> new CurriculumException(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));

        curriculumRepository.delete(session);
    }
}
