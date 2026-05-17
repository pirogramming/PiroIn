package com.example.Piroin.project.domain.question.service;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.curriculum.repository.CurriculumRepository;
import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.entity.*;
import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import com.example.Piroin.project.domain.question.exception.QuestionException;
import com.example.Piroin.project.domain.question.repository.*;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.repository.UserRepository;
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
@RequiredArgsConstructor
public class QuestionService {
    private static final int UNDERSTANDING_PAGE_SIZE = 1;
    private static final int POPULAR_LIKE_THRESHOLD = 5;

    private final QuestionRepository questionRepository;
    private final QuestionCommentRepository questionCommentRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final QuestionAnonymousIdentityRepository anonymousIdentityRepository;
    private final UnderstandingCheckRepository understandingCheckRepository;
    private final UnderstandingResponseRepository understandingResponseRepository;
    private final CurriculumRepository curriculumRepository;
    private final UserRepository userRepository;

    // 질문 방 조회
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

    // 질문 상세 조회
    @Transactional(readOnly = true)
    public QuestionResDTO.QuestionDetailResponse getQuestionDetail(Long questionId, Long userId) {
        User loginUser = findLoginUser(userId);
        Question question = findQuestion(questionId);
        return toDetailResponse(question, loginUser);
    }

    private QuestionResDTO.QuestionDetailResponse toDetailResponse(Question question, User loginUser) {
        boolean isLiked = questionLikeRepository.existsByQuestionAndUser(question, loginUser);
        boolean isPopular = !question.getIsResolved() && question.getLikeCount() >= POPULAR_LIKE_THRESHOLD;

        List<QuestionComment> topComments =
                questionCommentRepository.findByQuestionAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(question);

        List<QuestionResDTO.CommentResponse> commentResponses = topComments.stream()
                .map(comment -> toCommentResponse(question, comment))
                .toList();

        return new QuestionResDTO.QuestionDetailResponse(
                question.getId(), "작성자", question.getContent(), question.getImageUrl(),
                question.getIsResolved(), isPopular, question.getLikeCount(), isLiked,
                question.getCreatedAt(), commentResponses
        );
    }

    private QuestionResDTO.CommentResponse toCommentResponse(Question question, QuestionComment comment) {
        List<QuestionComment> replies =
                questionCommentRepository.findByParentCommentAndDeletedAtIsNullOrderByCreatedAtAsc(comment);

        List<QuestionResDTO.CommentResponse> replyResponses = replies.stream()
                .map(reply -> new QuestionResDTO.CommentResponse(
                        reply.getId(), getDisplayName(question, reply.getUser()),
                        reply.getContent(), reply.getImageUrl(), reply.getCreatedAt(), List.of()
                ))
                .toList();

        return new QuestionResDTO.CommentResponse(
                comment.getId(), getDisplayName(question, comment.getUser()),
                comment.getContent(), comment.getImageUrl(), comment.getCreatedAt(), replyResponses
        );
    }

    // 댓글 등록
    // POST /api/questions/{questionId}/comments
    @Transactional
    public QuestionResDTO.CommentCreateRes createComment(
            Long questionId,
            QuestionReqDTO.CommentReq request,
            Long userId
    ) {
        User loginUser = findLoginUser(userId);
        Question question = findQuestion(questionId);

        // 1. 대댓글 여부 확인: parentCommentId가 있으면 부모 댓글 조회
        QuestionComment parentComment = resolveParentComment(request.getParentCommentId());

        // 2. 댓글 엔티티 생성 및 저장
        LocalDateTime now = LocalDateTime.now();
        QuestionComment comment = QuestionComment.builder()
                .question(question)
                .user(loginUser)
                .parentComment(parentComment)  // 일반 댓글이면 null, 대댓글이면 부모 댓글
                .content(request.getContent())
                .createdAt(now)
                .updatedAt(now)
                .build();
        questionCommentRepository.save(comment);

        // 3. 해결된 질문에 댓글이 달리면 미해결로 자동 전환
        if (question.getIsResolved()) {
            question.markUnresolved();
        }

        // 4. 표시명 결정 (질문 작성자가 아닌 경우 익명 번호 부여)
        String displayName = assignAnonymousIdentity(question, loginUser);

        return new QuestionResDTO.CommentCreateRes(
                comment.getId(), question.getId(), displayName, comment.getContent(), comment.getCreatedAt()
        );
    }

    // parentCommentId가 있으면 해당 댓글 조회, 없으면 null 반환
    private QuestionComment resolveParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }
        return questionCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));
    }

    /*
    익명 번호 조회 또는 신규 부여
    
    - 질문 작성자 본인 → "작성자" 반환, 번호 부여 안 함
    - 이미 번호가 있는 유저 → 기존 번호 재사용
    - 처음 댓글 다는 유저 → 역할별 카운트 + 1로 새 번호 부여 후 DB 저장
    */
    private String assignAnonymousIdentity(Question question, User commenter) {
        // 질문 작성자 본인이면 번호 부여 없이 "작성자" 반환
        if (commenter.getId().equals(question.getUser().getId())) {
            return "작성자";
        }

        // 이미 이 질문에서 익명 번호가 있는지 확인
        return anonymousIdentityRepository
                .findByQuestionAndUser(question, commenter)
                .map(identity -> buildDisplayName(commenter.getRole(), identity.getAnonymousNo()))
                .orElseGet(() -> {
                    // 처음 댓글 다는 유저 → 역할별 카운트 기반으로 새 번호 부여
                    int nextNo = anonymousIdentityRepository
                            .countByQuestionAndUser_Role(question, commenter.getRole()) + 1;

                    anonymousIdentityRepository.save(QuestionAnonymousIdentity.builder()
                            .question(question)
                            .user(commenter)
                            .anonymousNo(nextNo)
                            .createdAt(LocalDateTime.now())
                            .build());

                    return buildDisplayName(commenter.getRole(), nextNo);
                });
    }

    // 역할 + 번호 → 표시명 변환
    private String buildDisplayName(Role role, int anonymousNo) {
        return role == Role.ADMIN ? "운영진" + anonymousNo : "익명" + anonymousNo;
    }

    // getDisplayName: 상세 조회 시 기존 익명 번호 읽기 (번호 부여 없음)
    private String getDisplayName(Question question, User commenter) {
        if (commenter.getId().equals(question.getUser().getId())) {
            return "작성자";
        }
        return anonymousIdentityRepository
                .findByQuestionAndUser(question, commenter)
                .map(identity -> buildDisplayName(commenter.getRole(), identity.getAnonymousNo()))
                .orElse(commenter.getRole() == Role.ADMIN ? "운영진" : "익명");
    }

    // 질문 등록
    @Transactional
    public QuestionResDTO.CreateRes createQuestion(Long sessionId, QuestionReqDTO.CreateReq request, Long userId) {
        User loginUser = findLoginUser(userId);
        StudySession session = findSession(sessionId);

        Question question = Question.builder()
                .session(session)
                .user(loginUser)
                .content(request.getContent())
                .isResolved(false)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return QuestionResDTO.CreateRes.from(questionRepository.save(question));
    }

    // 이해도 체크 응답
    @Transactional
    public QuestionResDTO.UnderstandingResponseResult respondUnderstandingCheck(
            Long sessionId, Long checkId, QuestionReqDTO.UnderstandingResponseReq request, Long userId
    ) {
        if (request == null || request.getChoice() == null) {
            throw new IllegalArgumentException("이해도 응답 선택지는 필수입니다.");
        }
        User loginUser = findLoginUser(userId);
        StudySession session = findSession(sessionId);
        UnderstandingCheck check = findUnderstandingCheck(checkId);
        validateCheckBelongsToSession(check, session);

        UnderstandResChoice selectedChoice = applyUnderstandingResponse(check, loginUser, request.getChoice());
        return toUnderstandingResponseResult(check, selectedChoice);
    }

    // 공통 헬퍼 메서드
    private User findLoginUser(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new QuestionException(HttpStatus.UNAUTHORIZED, "로그인 사용자를 찾을 수 없습니다."));
    }

    private Question findQuestion(Long questionId) {
        return questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다."));
    }

    private StudySession findSession(Long sessionId) {
        return curriculumRepository.findById(sessionId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));
    }

    private UnderstandingCheck findUnderstandingCheck(Long checkId) {
        return understandingCheckRepository.findById(checkId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "이해도 체크를 찾을 수 없습니다."));
    }

    private void validateCheckBelongsToSession(UnderstandingCheck check, StudySession session) {
        if (!check.getSession().getId().equals(session.getId())) {
            throw new IllegalArgumentException("해당 세션의 이해도 체크가 아닙니다.");
        }
    }

    private UnderstandResChoice applyUnderstandingResponse(
            UnderstandingCheck check, User loginUser, UnderstandResChoice requestedChoice
    ) {
        UnderstandingResponse response = understandingResponseRepository
                .findByCheckAndUser(check, loginUser).orElse(null);

        if (response == null) {
            LocalDateTime now = LocalDateTime.now();
            understandingResponseRepository.save(UnderstandingResponse.builder()
                    .check(check).user(loginUser).choice(requestedChoice)
                    .createdAt(now).updatedAt(now).build());
            return requestedChoice;
        }

        if (response.hasChoice(requestedChoice)) {
            understandingResponseRepository.delete(response);
            return null;
        }

        response.changeChoice(requestedChoice);
        return requestedChoice;
    }

    private QuestionResDTO.UnderstandingResponseResult toUnderstandingResponseResult(
            UnderstandingCheck check, UnderstandResChoice selectedChoice
    ) {
        return new QuestionResDTO.UnderstandingResponseResult(
                check.getId(), selectedChoice,
                understandingResponseRepository.countByCheckAndChoice(check, UnderstandResChoice.UNDERSTOOD),
                understandingResponseRepository.countByCheckAndChoice(check, UnderstandResChoice.NOT_UNDERSTOOD)
        );
    }

    private QuestionResDTO.SessionResponse toSessionResponse(StudySession session) {
        return new QuestionResDTO.SessionResponse(
                session.getId(), session.getWeek().intValue(),
                session.getSessionDate().getDayOfWeek().name(),
                session.getDayPart().name(), session.getSessionDate(), session.getTitle()
        );
    }

    private QuestionResDTO.UnderstandingSliceResponse getUnderstandingSlice(StudySession session, int understandingIndex) {
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
                toUnderstandingCheckResponse(current), understandingIndex, totalCount,
                understandingIndex < totalCount - 1, understandingIndex > 0
        );
    }

    private QuestionResDTO.UnderstandingCheckResponse toUnderstandingCheckResponse(UnderstandingCheck check) {
        return new QuestionResDTO.UnderstandingCheckResponse(
                check.getId(), check.getTitle(), check.getDescription(),
                understandingResponseRepository.countByCheckAndChoice(check, UnderstandResChoice.UNDERSTOOD),
                understandingResponseRepository.countByCheckAndChoice(check, UnderstandResChoice.NOT_UNDERSTOOD),
                check.getCreatedAt()
        );
    }

    private QuestionResDTO.QuestionGroupsResponse getQuestionGroups(StudySession session) {
        List<Question> questions = questionRepository.findBySessionAndDeletedAtIsNull(session);

        List<QuestionResDTO.QuestionSummaryResponse> popularQuestions = questions.stream()
                .filter(q -> !q.getIsResolved() && q.getLikeCount() >= POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator.comparing(Question::getLikeCount, Comparator.reverseOrder())
                        .thenComparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse).toList();

        List<QuestionResDTO.QuestionSummaryResponse> unresolvedQuestions = questions.stream()
                .filter(q -> !q.getIsResolved() && q.getLikeCount() < POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse).toList();

        List<QuestionResDTO.QuestionSummaryResponse> resolvedQuestions = questions.stream()
                .filter(Question::getIsResolved)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toQuestionSummaryResponse).toList();

        return new QuestionResDTO.QuestionGroupsResponse(popularQuestions, unresolvedQuestions, resolvedQuestions);
    }

    private QuestionResDTO.QuestionSummaryResponse toQuestionSummaryResponse(Question question) {
        return new QuestionResDTO.QuestionSummaryResponse(
                question.getId(), question.getContent(), question.getImageUrl(),
                question.getIsResolved(),
                !question.getIsResolved() && question.getLikeCount() >= POPULAR_LIKE_THRESHOLD,
                question.getLikeCount(),
                questionCommentRepository.countByQuestionAndDeletedAtIsNull(question),
                question.getCreatedAt()
        );
    }
}
