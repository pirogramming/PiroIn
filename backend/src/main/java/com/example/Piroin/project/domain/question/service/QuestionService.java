package com.example.Piroin.project.domain.question.service;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.exception.QuestionException;
import com.example.Piroin.project.domain.question.repository.QuestionRepository;
import com.example.Piroin.project.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // final 필드를 생성자 주입으로 자동 처리
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CurriculumRepository curriculumRepository;

    /*
    질문 등록
    
    @param sessionId  질문이 달릴 세션 ID
    @param request    질문 내용 (content)
    @param loginUser  현재 로그인된 유저
    */
    @Transactional
    public QuestionResDTO.CreateRes createQuestion(
            Long sessionId,
            QuestionReqDTO.CreateReq request,
            User loginUser
    ) {
        // 1. 세션 존재 여부 확인
        StudySession session = curriculumRepository.findById(sessionId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));

        // 2. 질문 엔티티 생성
        Question question = Question.builder()
                .session(session)
                .user(loginUser)
                .content(request.getContent())
                .isResolved(false)   // 등록 시 기본값: 미해결
                .likeCount(0)        // 등록 시 기본값: 좋아요 0
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 3. DB 저장 후 DTO 변환하여 반환
        return QuestionResDTO.CreateRes.from(questionRepository.save(question));
    }
}