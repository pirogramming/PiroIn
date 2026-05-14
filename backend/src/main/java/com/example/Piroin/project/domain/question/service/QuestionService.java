package com.example.Piroin.project.domain.question.service;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.entity.UnderstandingCheck;
import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import com.example.Piroin.project.domain.question.exception.QuestionException;
import com.example.Piroin.project.domain.question.repository.QuestionCommentRepository;
import com.example.Piroin.project.domain.question.repository.QuestionRepository;
import com.example.Piroin.project.domain.question.repository.UnderstandingCheckRepository;
import com.example.Piroin.project.domain.question.repository.UnderstandingResponseRepository;
import com.example.Piroin.project.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor // final 필드를 생성자 주입으로 자동 처리
public class QuestionService {
    private static final int UNDERSTANDING_PAGE_SIZE = 1;
    private static final int POPULAR_LIKE_THRESHOLD = 5;

    private final QuestionRepository questionRepository;
    private final QuestionCommentRepository questionCommentRepository;
    private final UnderstandingCheckRepository understandingCheckRepository;
    private final UnderstandingResponseRepository understandingResponseRepository;
    private final CurriculumRepository curriculumRepository;

    @Transactional(readOnly = true)
    public QuestionResDTO.QuestionRoomResponse getQuestionRoom(Long sessionId, int understandingIndex) {
        if (understandingIndex < 0) {
            throw new IllegalArgumentException("이해도 조회 인덱스는 0 이상이어야 합니다.");
        }

        StudySession session = findSession(sessionId);

        return new QuestionResDTO.QuestionRoomResponse(
                toSessionResponse(session),
                getUnderstandingSlice(session, understandingIndex),
                getQuestionGroups(session)
        );
    }

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
        StudySession session = findSession(sessionId);

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

    private StudySession findSession(Long sessionId) {
        return curriculumRepository.findById(sessionId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));
    }

    private QuestionResDTO.SessionResponse toSessionResponse(StudySession session) {
        return new QuestionResDTO.SessionResponse(
                session.getId(),
                session.getWeek().intValue(),
                session.getSessionDate().getDayOfWeek().name(),
                session.getDayPart().name(),
                session.getSessionDate(),
                session.getTitle()
        );
    }

    private QuestionResDTO.UnderstandingSliceResponse getUnderstandingSlice(StudySession session, int understandingIndex) {
        // 이해도 체크는 최신순으로 정렬하고, 화면에서는 한 번에 하나씩 넘겨본다.
        Page<UnderstandingCheck> understandingPage = understandingCheckRepository
                .findBySessionOrderByCreatedAtDesc(session, PageRequest.of(understandingIndex, UNDERSTANDING_PAGE_SIZE));

        int totalCount = (int) understandingPage.getTotalElements();
        if (totalCount == 0) {
            return new QuestionResDTO.UnderstandingSliceResponse(null, 0, 0, false, false);
        }

        if (understandingPage.getContent().isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 이해도 조회 인덱스입니다.");
        }

        UnderstandingCheck current = understandingPage.getContent().get(0);
        return new QuestionResDTO.UnderstandingSliceResponse(
                toUnderstandingCheckResponse(current),
                understandingIndex,
                totalCount,
                understandingIndex < totalCount - 1,
                understandingIndex > 0
        );
    }

    private QuestionResDTO.UnderstandingCheckResponse toUnderstandingCheckResponse(UnderstandingCheck check) {
        int understoodCount = understandingResponseRepository
                .countByCheckAndChoice(check, UnderstandResChoice.UNDERSTOOD);
        int notUnderstoodCount = understandingResponseRepository
                .countByCheckAndChoice(check, UnderstandResChoice.NOT_UNDERSTOOD);

        return new QuestionResDTO.UnderstandingCheckResponse(
                check.getId(),
                check.getTitle(),
                check.getDescription(),
                understoodCount,
                notUnderstoodCount,
                check.getCreatedAt()
        );
    }

    private QuestionResDTO.QuestionGroupsResponse getQuestionGroups(StudySession session) {
        List<Question> questions = questionRepository.findBySessionAndDeletedAtIsNull(session);

        // 좋아요 5개 이상인 미해결 질문은 질문방 상단 고정 영역에 먼저 노출한다.
        List<QuestionResDTO.QuestionSummaryResponse> popularQuestions = questions.stream()
                .filter(question -> !question.getIsResolved())
                .filter(question -> question.getLikeCount() >= POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator
                        .comparing(Question::getLikeCount, Comparator.reverseOrder())
                        .thenComparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse)
                .toList();

        // 일반 미해결 질문은 인기 질문 아래에 최신순으로 노출한다.
        List<QuestionResDTO.QuestionSummaryResponse> unresolvedQuestions = questions.stream()
                .filter(question -> !question.getIsResolved())
                .filter(question -> question.getLikeCount() < POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse)
                .toList();

        // 해결된 질문은 미해결 질문과 섞지 않고 별도 영역에서 최신순으로 노출한다.
        List<QuestionResDTO.QuestionSummaryResponse> resolvedQuestions = questions.stream()
                .filter(Question::getIsResolved)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse)
                .toList();

        return new QuestionResDTO.QuestionGroupsResponse(popularQuestions, unresolvedQuestions, resolvedQuestions);
    }

    private QuestionResDTO.QuestionSummaryResponse toQuestionSummaryResponse(Question question) {
        // 질문방 목록은 댓글 본문을 포함하지 않고 개수만 내려준다.
        // 댓글 목록은 특정 질문 상세 조회 API에서 조회한다.
        return new QuestionResDTO.QuestionSummaryResponse(
                question.getId(),
                question.getContent(),
                question.getImageUrl(),
                question.getIsResolved(),
                !question.getIsResolved() && question.getLikeCount() >= POPULAR_LIKE_THRESHOLD,
                question.getLikeCount(),
                questionCommentRepository.countByQuestionAndDeletedAtIsNull(question),
                question.getCreatedAt()
        );
    }
}
