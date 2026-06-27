package com.example.Piroin.project.domain.question.service;

import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QuestionEventService {
    private static final long SSE_TIMEOUT_MILLIS = 3L * 60L * 1000L;

    // sessionId별로 현재 질문방을 보고 있는 SSE 연결들을 보관한다.
    private final Map<Long, List<SseEmitter>> sessionEmitters = new ConcurrentHashMap<>();

    // 클라이언트가 질문방에 들어오면 SSE 연결을 열고 해당 세션 구독자로 등록한다.
    public SseEmitter subscribe(Long sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        sessionEmitters.computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        // 페이지 이탈, 타임아웃, 네트워크 오류 시 죽은 연결이 남지 않도록 제거한다.
        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(error -> removeEmitter(sessionId, emitter));

        try {
            // 최초 연결 확인용 이벤트. 프론트는 이 이벤트로 구독 성공을 확인할 수 있다.
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("connected"));
        } catch (IOException | IllegalStateException e) {
            removeEmitter(sessionId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // 댓글 생성 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishCommentCreated(Long sessionId, QuestionResDTO.CommentCreatedEvent event) {
        broadcast(sessionId, "comment-created", event);
    }

    // 댓글 수정/삭제 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishCommentUpdated(Long sessionId, QuestionResDTO.CommentUpdatedEvent event) {
        broadcast(sessionId, "comment-updated", event);
    }

    // 질문 등록 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishQuestionCreated(Long sessionId, QuestionResDTO.QuestionCreatedEvent event) {
        broadcast(sessionId, "question-created", event);
    }

    // 질문 상태 변경 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishQuestionUpdated(Long sessionId, QuestionResDTO.QuestionUpdatedEvent event) {
        broadcast(sessionId, "question-updated", event);
    }

    // 운영진 확인 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishQuestionChecked(Long sessionId, QuestionResDTO.QuestionCheckedEvent event) {
        broadcast(sessionId, "question-checked", event);
    }

    // 이해도 체크 생성 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishUnderstandingCheckCreated(Long sessionId, QuestionResDTO.UnderstandingCheckCreatedEvent event) {
        broadcast(sessionId, "understanding-check-created", event);
    }

    // 이해도 응답(O/X) 업데이트 이벤트를 같은 세션 질문방을 구독 중인 모든 클라이언트에게 전파한다.
    public void publishUnderstandingResponseUpdated(Long sessionId, QuestionResDTO.UnderstandingResponseUpdatedEvent event) {
        broadcast(sessionId, "understanding-response-updated", event);
    }

    // 지정한 이벤트 이름과 데이터를 해당 세션의 모든 구독자에게 전송한다.
    // 전송 실패한 연결은 즉시 제거한다.
    private void broadcast(Long sessionId, String eventName, Object data) {
        List<SseEmitter> emitters = sessionEmitters.getOrDefault(sessionId, List.of());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException | IllegalStateException e) {
                removeEmitter(sessionId, emitter);
                emitter.completeWithError(e);
            }
        }
    }

    // 더 이상 사용하지 않는 연결을 제거하고, 세션에 남은 연결이 없으면 세션 키도 정리한다.
    private void removeEmitter(Long sessionId, SseEmitter emitter) {
        List<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            sessionEmitters.remove(sessionId);
        }
    }
}
