package com.example.Piroin.project.domain.question.service;

import com.example.Piroin.project.domain.attendance.service.AttendanceService;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final QuestionEventService questionEventService;
    private final AttendanceService attendanceService;

    // 질문 방 조회
    @Transactional(readOnly = true)
    public QuestionResDTO.QuestionRoomResponse getQuestionRoom(Long sessionId, int understandingIndex, Long userId) {
        if (understandingIndex < 0) {
            throw new IllegalArgumentException("이해도 조회 인덱스는 0 이상이어야 합니다.");
        }
        StudySession session = findSession(sessionId);
        User loginUser = findLoginUser(userId);
        return new QuestionResDTO.QuestionRoomResponse(
                toSessionResponse(session),
                getUnderstandingSlice(session, understandingIndex),
                getQuestionGroups(session, loginUser)
        );
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribeQuestionEvents(Long sessionId) {
        // 존재하는 세션에 대해서만 SSE 연결을 허용한다.
        findSession(sessionId);
        return questionEventService.subscribe(sessionId);
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
        boolean isMine = question.getUser().getId().equals(loginUser.getId());
        boolean isPopular = !question.getIsResolved() && question.getLikeCount() >= POPULAR_LIKE_THRESHOLD;

        List<QuestionComment> topComments =
                questionCommentRepository.findByQuestionAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(question);

        List<QuestionResDTO.CommentResponse> commentResponses = topComments.stream()
                .map(comment -> toCommentResponse(question, comment, loginUser))
                .toList();

        return new QuestionResDTO.QuestionDetailResponse(
                question.getId(), "작성자", question.getContent(), question.getImageUrl(),
                question.getIsResolved(), isPopular, question.getLikeCount(), isLiked,
                isMine,
                question.getCreatedAt(), commentResponses
        );
    }

    private QuestionResDTO.CommentResponse toCommentResponse(Question question, QuestionComment comment, User loginUser) {
        List<QuestionComment> replies =
                questionCommentRepository.findByParentCommentAndDeletedAtIsNullOrderByCreatedAtAsc(comment);

        List<QuestionResDTO.CommentResponse> replyResponses = replies.stream()
                .map(reply -> new QuestionResDTO.CommentResponse(
                        reply.getId(), getDisplayName(question, reply.getUser()),
                        reply.getContent(), reply.getImageUrl(), isCommentMine(reply, loginUser),
                        reply.getCreatedAt(), List.of()
                ))
                .toList();

        return new QuestionResDTO.CommentResponse(
                comment.getId(), getDisplayName(question, comment.getUser()),
                comment.getContent(), comment.getImageUrl(), isCommentMine(comment, loginUser),
                comment.getCreatedAt(), replyResponses
        );
    }

    private boolean isCommentMine(QuestionComment comment, User loginUser) {
        return comment.getUser().getId().equals(loginUser.getId());
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
        QuestionComment parentComment = resolveParentComment(request.getParentCommentId(), question);

        // 2. 댓글 엔티티 생성 및 저장
        LocalDateTime now = LocalDateTime.now();
        QuestionComment comment = QuestionComment.builder()
                .question(question)
                .user(loginUser)
                .parentComment(parentComment)  // 일반 댓글이면 null, 대댓글이면 부모 댓글
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
        questionCommentRepository.save(comment);

        // 수동 해결/미해결 변경은 운영진 권한이 필요하지만,
        // 댓글 작성으로 인한 미해결 전환은 권한 API가 아니라 서버 내부 도메인 규칙이다.
        if (question.getIsResolved()) {
            question.markUnresolved();
        }

        // 4. 표시명 결정 (질문 작성자가 아닌 경우 익명 번호 부여)
        String displayName = assignAnonymousIdentity(question, loginUser);

        QuestionResDTO.CommentCreateRes response = new QuestionResDTO.CommentCreateRes(
                comment.getId(), question.getId(), displayName,
                comment.getContent(), question.getIsResolved(), comment.getCreatedAt()
        );

        // DB 반영이 끝난 뒤 같은 질문방 구독자들이 목록 댓글 미리보기를 갱신하도록 알린다.
        publishCommentCreatedEventAfterCommit(question);

        return response;
    }

    // parentCommentId가 있으면 해당 댓글 조회, 없으면 null 반환
    private QuestionComment resolveParentComment(Long parentCommentId, Question question) {
        if (parentCommentId == null) {
            return null;
        }
        QuestionComment parent = questionCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));

        // 삭제된 댓글에는 대댓글을 달 수 없음
        if (parent.getDeletedAt() != null) {
            throw new QuestionException(HttpStatus.BAD_REQUEST, "삭제된 댓글에는 대댓글을 달 수 없습니다.");
        }
        // 다른 질문의 댓글을 부모로 붙이는 것 방지
        if (!parent.getQuestion().getId().equals(question.getId())) {
            throw new QuestionException(HttpStatus.BAD_REQUEST, "다른 질문의 댓글에는 대댓글을 달 수 없습니다.");
        }
        // 대댓글에 또 대댓글을 다는 것 방지 (2depth 제한)
        if (parent.getParentComment() != null) {
            throw new QuestionException(HttpStatus.BAD_REQUEST, "대댓글에는 대댓글을 달 수 없습니다.");
        }

        return parent;
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
                            .findMaxAnonymousNoByQuestionAndRole(question, commenter.getRole()) + 1;

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
                .imageUrl(request.getImageUrl())
                .isResolved(false)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Question saved = questionRepository.save(question);

        // DB 반영 후 같은 세션을 보고 있는 모든 클라이언트에게 새 질문을 알림
        publishQuestionCreatedEventAfterCommit(saved);

        return QuestionResDTO.CreateRes.from(saved);
    }

    // 좋아요 토글
    // POST /api/questions/{questionId}/like
    @Transactional
    public QuestionResDTO.LikeRes toggleLike(Long questionId, Long userId) {
        User loginUser = findLoginUser(userId);
        Question question = findQuestion(questionId);

        // 이미 좋아요를 눌렀는지 확인
        return questionLikeRepository.findByQuestionAndUser(question, loginUser)
                .map(existingLike -> {
                    // 이미 눌렀으면 → 취소 (삭제 + likeCount -1)
                    questionLikeRepository.delete(existingLike);
                    question.decreaseLikeCount();
                    return new QuestionResDTO.LikeRes(question.getId(), question.getLikeCount(), false);
                })
                .orElseGet(() -> {
                    // 처음 누르면 → 추가 (저장 + likeCount +1)
                    questionLikeRepository.save(QuestionLike.builder()
                            .question(question)
                            .user(loginUser)
                            .createdAt(LocalDateTime.now())
                            .build());
                    question.increaseLikeCount();
                    return new QuestionResDTO.LikeRes(question.getId(), question.getLikeCount(), true);
                });
    }

    // 질문 수정
    @Transactional
    public QuestionResDTO.UpdateDeleteRes updateQuestion(
            Long questionId,
            QuestionReqDTO.UpdateReq request,
            Long userId
    ) {
        User loginUser = findLoginUser(userId);
        Question question = findQuestion(questionId);
        validateQuestionOwner(question, loginUser);

        question.updateContent(request.getContent());

        return new QuestionResDTO.UpdateDeleteRes(
                question.getId(), question.getContent(),
                question.getUpdatedAt(), question.getDeletedAt()
        );
    }

    // 질문 삭제
    @Transactional
    public QuestionResDTO.UpdateDeleteRes deleteQuestion(Long questionId, Long userId) {
        User loginUser = findLoginUser(userId);
        Question question = findQuestion(questionId);
        validateQuestionOwner(question, loginUser);

        question.softDelete();

        return new QuestionResDTO.UpdateDeleteRes(
                question.getId(), question.getContent(),
                question.getUpdatedAt(), question.getDeletedAt()
        );
    }

    // 댓글 수정
    @Transactional
    public QuestionResDTO.CommentUpdateDeleteRes updateComment(
            Long commentId,
            QuestionReqDTO.CommentUpdateReq request,
            Long userId
    ) {
        User loginUser = findLoginUser(userId);
        QuestionComment comment = findComment(commentId);
        validateCommentOwner(comment, loginUser);

        comment.updateContent(request.getContent());

        return new QuestionResDTO.CommentUpdateDeleteRes(
                comment.getId(), comment.getContent(),
                comment.getUpdatedAt(), comment.getDeletedAt()
        );
    }

    // 댓글 삭제
    @Transactional
    public QuestionResDTO.CommentUpdateDeleteRes deleteComment(Long commentId, Long userId) {
        User loginUser = findLoginUser(userId);
        QuestionComment comment = findComment(commentId);
        validateCommentOwner(comment, loginUser);

        comment.softDelete();

        return new QuestionResDTO.CommentUpdateDeleteRes(
                comment.getId(), comment.getContent(),
                comment.getUpdatedAt(), comment.getDeletedAt()
        );
    }

    // 질문 상태 완료 전환
    // PATCH /api/questions/{questionId}/status
    @Transactional
    public QuestionResDTO.StatusUpdateRes updateQuestionStatus(Long questionId, Long userId) {
        User loginUser = findLoginUser(userId);
        // 사용자가 직접 상태를 바꾸는 수동 해결 처리는 운영진만 가능하다.
        validateAdmin(loginUser);

        Question question = findQuestion(questionId);
        question.markResolved();

        return new QuestionResDTO.StatusUpdateRes(
                question.getId(), question.getIsResolved(), question.getUpdatedAt()
        );
    }

    // 이해도 체크 생성
    @Transactional
    public QuestionResDTO.UnderstandingCheckCreateResponse createUnderstandingCheck(
            Long sessionId, QuestionReqDTO.UnderstandingCheckCreateReq request, Long userId
    ) {
        validateUnderstandingCheckCreateRequest(request);
        User loginUser = findLoginUser(userId);
        validateAdmin(loginUser);
        StudySession session = findSession(sessionId);

        LocalDateTime now = LocalDateTime.now();
        UnderstandingCheck check = understandingCheckRepository.save(UnderstandingCheck.builder()
                .session(session)
                .createdBy(loginUser)
                .title(request.getContent().trim())
                .createdAt(now)
                .updatedAt(now)
                .build());

        int attendanceCount = attendanceService.countAttendedBySession(session);

        // DB 반영 후 같은 세션을 보고 있는 모든 클라이언트에게 새 이해도 체크를 알림
        publishUnderstandingCheckCreatedEventAfterCommit(session.getId(), check, attendanceCount);

        return new QuestionResDTO.UnderstandingCheckCreateResponse(
                check.getId(), check.getTitle(), 0, null, 0, 0, check.getCreatedAt()
        );
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
        // O/X 클릭 직후 프론트가 13/29와 O/X 뱃지를 바로 갱신할 수 있도록 최신 분모도 함께 내려준다.
        int attendanceCount = attendanceService.countAttendedBySession(session);
        QuestionResDTO.UnderstandingResponseResult result = toUnderstandingResponseResult(check, selectedChoice, attendanceCount);

        // DB 반영 후 같은 세션을 보고 있는 모든 클라이언트의 이해도 카운트를 갱신
        publishUnderstandingResponseUpdatedEventAfterCommit(sessionId, result);

        return result;
    }

    // 공통 헬퍼 메서드
    private User findLoginUser(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new QuestionException(HttpStatus.UNAUTHORIZED, "로그인 사용자를 찾을 수 없습니다."));
    }

    private void validateAdmin(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new QuestionException(HttpStatus.FORBIDDEN, "관리자만 사용할 수 있는 기능입니다.");
        }
    }

    private void validateUnderstandingCheckCreateRequest(QuestionReqDTO.UnderstandingCheckCreateReq request) {
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("이해도 체크 내용은 필수입니다.");
        }
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

    private void validateQuestionOwner(Question question, User loginUser) {
        if (!question.getUser().getId().equals(loginUser.getId())) {
            throw new QuestionException(HttpStatus.FORBIDDEN, "본인의 질문만 수정/삭제할 수 있습니다.");
        }
    }

    private void validateCommentOwner(QuestionComment comment, User loginUser) {
        if (!comment.getUser().getId().equals(loginUser.getId())) {
            throw new QuestionException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정/삭제할 수 있습니다.");
        }
    }

    private QuestionComment findComment(Long commentId) {
        return questionCommentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new QuestionException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
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
            // 같은 O/X 버튼을 다시 누르면 인스타 좋아요 취소처럼 응답을 삭제하고 selectedChoice는 null로 내려간다.
            return null;
        }

        response.changeChoice(requestedChoice);
        return requestedChoice;
    }

    private QuestionResDTO.UnderstandingResponseResult toUnderstandingResponseResult(
            UnderstandingCheck check, UnderstandResChoice selectedChoice, Integer attendanceCount
    ) {
        // respondedCount는 프론트 화면의 "13/29" 중 13에 해당한다.
        int understoodCount = understandingResponseRepository.countByCheckAndChoice(
                check, UnderstandResChoice.UNDERSTOOD
        );
        int notUnderstoodCount = understandingResponseRepository.countByCheckAndChoice(
                check, UnderstandResChoice.NOT_UNDERSTOOD
        );

        return new QuestionResDTO.UnderstandingResponseResult(
                check.getId(), selectedChoice,
                understoodCount + notUnderstoodCount,
                attendanceCount,
                understoodCount,
                notUnderstoodCount
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
        // attendanceCount는 프론트 화면의 "13/29" 중 29에 해당한다.
        int attendanceCount = attendanceService.countAttendedBySession(session);
        return new QuestionResDTO.UnderstandingSliceResponse(
                toUnderstandingCheckResponse(current, attendanceCount), understandingIndex, totalCount,
                understandingIndex < totalCount - 1, understandingIndex > 0
        );
    }

    private QuestionResDTO.UnderstandingCheckResponse toUnderstandingCheckResponse(UnderstandingCheck check) {
        return toUnderstandingCheckResponse(check, null);
    }

    private QuestionResDTO.UnderstandingCheckResponse toUnderstandingCheckResponse(
            UnderstandingCheck check, Integer attendanceCount
    ) {
        // understoodCount/notUnderstoodCount는 오른쪽 O/X 뱃지 숫자로 그대로 사용한다.
        int understoodCount = understandingResponseRepository.countByCheckAndChoice(
                check, UnderstandResChoice.UNDERSTOOD
        );
        int notUnderstoodCount = understandingResponseRepository.countByCheckAndChoice(
                check, UnderstandResChoice.NOT_UNDERSTOOD
        );

        return new QuestionResDTO.UnderstandingCheckResponse(
                check.getId(), check.getTitle(),
                understoodCount + notUnderstoodCount,
                attendanceCount,
                understoodCount,
                notUnderstoodCount,
                check.getCreatedAt()
        );
    }

    private QuestionResDTO.QuestionGroupsResponse getQuestionGroups(StudySession session, User loginUser) {
        List<Question> questions = questionRepository.findBySessionAndDeletedAtIsNull(session);
        QuestionSummaryContext summaryContext = getQuestionSummaryContext(questions);

        List<QuestionResDTO.QuestionSummaryResponse> popularQuestions = questions.stream()
                .filter(q -> !q.getIsResolved() && q.getLikeCount() >= POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator.comparing(Question::getLikeCount, Comparator.reverseOrder())
                        .thenComparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(q -> toQuestionSummaryResponse(q, summaryContext, loginUser)).toList();

        List<QuestionResDTO.QuestionSummaryResponse> unresolvedQuestions = questions.stream()
                .filter(q -> !q.getIsResolved() && q.getLikeCount() < POPULAR_LIKE_THRESHOLD)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(q -> toQuestionSummaryResponse(q, summaryContext, loginUser)).toList();

        List<QuestionResDTO.QuestionSummaryResponse> resolvedQuestions = questions.stream()
                .filter(Question::getIsResolved)
                .sorted(Comparator.comparing(Question::getCreatedAt, Comparator.reverseOrder()))
                .map(q -> toQuestionSummaryResponse(q, summaryContext, loginUser)).toList();

        return new QuestionResDTO.QuestionGroupsResponse(popularQuestions, unresolvedQuestions, resolvedQuestions);
    }

    private QuestionResDTO.QuestionSummaryResponse toQuestionSummaryResponse (
            Question question,
            QuestionSummaryContext summaryContext,
            User loginUser
    ) {
        Long questionId = question.getId();
        boolean isLiked = questionLikeRepository.existsByQuestionAndUser(question, loginUser);
        boolean isMine = question.getUser().getId().equals(loginUser.getId());
        return new QuestionResDTO.QuestionSummaryResponse(
                questionId, question.getContent(), question.getImageUrl(),
                question.getIsResolved(),
                !question.getIsResolved() && question.getLikeCount() >= POPULAR_LIKE_THRESHOLD,
                isLiked,
                isMine,
                question.getLikeCount(),
                summaryContext.commentCounts().getOrDefault(questionId, 0),
                // 목록 화면은 최상위 댓글 중 먼저 달린 3개만 미리보기로 보여준다.
                summaryContext.previewComments().getOrDefault(questionId, List.of()),
                question.getCreatedAt()
        );
    }

    private QuestionSummaryContext getQuestionSummaryContext(List<Question> questions) {
        if (questions.isEmpty()) {
            return new QuestionSummaryContext(Map.of(), Map.of());
        }

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();
        Map<Long, Question> questionsById = questions.stream()
                .collect(Collectors.toMap(Question::getId, question -> question));

        Map<Long, Integer> commentCounts = new HashMap<>();
        questionCommentRepository.countByQuestionIds(questionIds)
                .forEach(row -> commentCounts.put(row.getQuestionId(), Math.toIntExact(row.getCommentCount())));

        Map<Long, List<QuestionResDTO.PreviewCommentResponse>> previewComments = new HashMap<>();
        questionCommentRepository.findPreviewCommentsByQuestionIds(questionIds)
                .forEach(row -> {
                    Question question = questionsById.get(row.getQuestionId());
                    if (question == null) {
                        return;
                    }
                    previewComments.computeIfAbsent(row.getQuestionId(), key -> new ArrayList<>())
                            .add(toPreviewCommentResponse(question, row));
                });

        return new QuestionSummaryContext(commentCounts, previewComments);
    }

    private QuestionResDTO.PreviewCommentResponse toPreviewCommentResponse(
            Question question,
            QuestionCommentRepository.PreviewCommentRow row
    ) {
        return new QuestionResDTO.PreviewCommentResponse(
                row.getCommentId(),
                getPreviewDisplayName(question, row),
                row.getContent(),
                hasPreviewImage(row),
                row.getCreatedAt()
        );
    }

    private boolean hasPreviewImage(QuestionCommentRepository.PreviewCommentRow row) {
        return row.getImageUrl() != null && !row.getImageUrl().isBlank();
    }

    private String getPreviewDisplayName(Question question, QuestionCommentRepository.PreviewCommentRow row) {
        if (row.getUserId().equals(question.getUser().getId())) {
            return "작성자";
        }
        if (row.getAnonymousNo() == null) {
            Role role = Role.valueOf(row.getUserRole());
            return role == Role.ADMIN ? "운영진" : "익명";
        }
        return buildDisplayName(Role.valueOf(row.getUserRole()), row.getAnonymousNo());
    }

    private void publishCommentCreatedEventAfterCommit(Question question) {
        Long sessionId = question.getSession().getId();
        Long questionId = question.getId();
        QuestionSummaryContext summaryContext = getQuestionSummaryContext(List.of(question));

        // 프론트가 전체 목록을 다시 조회하지 않고 해당 질문만 갱신할 수 있는 최소 데이터만 보낸다.
        QuestionResDTO.CommentCreatedEvent event = new QuestionResDTO.CommentCreatedEvent(
                "COMMENT_CREATED",
                sessionId,
                questionId,
                question.getIsResolved(),
                summaryContext.commentCounts().getOrDefault(questionId, 0),
                summaryContext.previewComments().getOrDefault(questionId, List.of())
        );

        publishAfterCommit(() -> questionEventService.publishCommentCreated(sessionId, event));
    }

    private void publishQuestionCreatedEventAfterCommit(Question question) {
        Long sessionId = question.getSession().getId();

        QuestionResDTO.QuestionCreatedEvent event = new QuestionResDTO.QuestionCreatedEvent(
                "QUESTION_CREATED",
                sessionId,
                question.getId(),
                question.getContent(),
                question.getImageUrl(),
                question.getLikeCount(),
                0,  // 방금 만들어진 질문이므로 댓글 수는 0
                question.getCreatedAt()
        );

        publishAfterCommit(() -> questionEventService.publishQuestionCreated(sessionId, event));
    }

    private void publishUnderstandingCheckCreatedEventAfterCommit(
            Long sessionId, UnderstandingCheck check, int attendanceCount
    ) {
        QuestionResDTO.UnderstandingCheckCreatedEvent event = new QuestionResDTO.UnderstandingCheckCreatedEvent(
                "UNDERSTANDING_CHECK_CREATED",
                sessionId,
                check.getId(),
                check.getTitle(),
                0,               // 생성 직후 응답 수 0
                attendanceCount,
                0,               // 생성 직후 O 0
                0,               // 생성 직후 X 0
                check.getCreatedAt()
        );

        publishAfterCommit(() -> questionEventService.publishUnderstandingCheckCreated(sessionId, event));
    }

    private void publishUnderstandingResponseUpdatedEventAfterCommit(
            Long sessionId, QuestionResDTO.UnderstandingResponseResult result
    ) {
        QuestionResDTO.UnderstandingResponseUpdatedEvent event = new QuestionResDTO.UnderstandingResponseUpdatedEvent(
                "UNDERSTANDING_RESPONSE_UPDATED",
                sessionId,
                result.checkId(),
                result.respondedCount(),
                result.attendanceCount(),
                result.understoodCount(),
                result.notUnderstoodCount()
        );

        publishAfterCommit(() -> questionEventService.publishUnderstandingResponseUpdated(sessionId, event));
    }

    // 롤백된 댓글이 실시간 화면에 먼저 보이지 않도록, 활성화된 트랜잭션 동기화 안에서만 커밋 이후 이벤트를 발행한다.
    private void publishAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("publishAfterCommit must be called within an active transaction synchronization");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private record QuestionSummaryContext(
            Map<Long, Integer> commentCounts,
            Map<Long, List<QuestionResDTO.PreviewCommentResponse>> previewComments
    ) {
    }
}
