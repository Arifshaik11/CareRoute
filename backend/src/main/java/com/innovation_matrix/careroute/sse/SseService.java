package com.innovation_matrix.careroute.sse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {

    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    private static final long TIMEOUT_MS = 300_000L; // 5 minutes

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> emitters.remove(userId, emitter));
        emitter.onError(e -> emitters.remove(userId, emitter));
        try {
            emitter.send(SseEmitter.event().name("ping").data("connected", MediaType.TEXT_PLAIN));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }
        return emitter;
    }

    /** Push an event to all connected clients. */
    public void pushToAll(String eventName, Object data) {
        if (emitters.isEmpty()) return;
        var dead = new ArrayList<Long>();
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                dead.add(userId);
            }
        });
        dead.forEach(emitters::remove);
        if (!dead.isEmpty()) {
            log.debug("Removed {} stale SSE emitter(s)", dead.size());
        }
    }
}
