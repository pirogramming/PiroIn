package com.example.Piroin.project.domain.curriculum.service;

import com.example.Piroin.project.domain.curriculum.converter.CurriculumConverter;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
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

    @Transactional
    public CurriculumResDTO.CreateSessionRes createSession(CurriculumReqDTO.CreateSessionReq req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new CurriculumException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        StudySession session = CurriculumConverter.toStudySession(req, user);
        StudySession savedSession = curriculumRepository.save(session);

        return CurriculumConverter.toCreateSessionRes(savedSession);
    }
}
