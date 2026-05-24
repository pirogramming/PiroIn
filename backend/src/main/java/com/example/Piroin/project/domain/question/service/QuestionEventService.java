package com.example.Piroin.project.domain.question.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QuestionEventService {
    private static final long SSE_TIMEOUT_MILLIS = 60L * 60L * 1000L;

    private final Map<Long, List<SseEmitter>> sessionEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        sessionEmitters.computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(error -> removeEmitter(sessionId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("connected"));
        } catch (IOException | IllegalStateException e) {
            removeEmitter(sessionId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

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
