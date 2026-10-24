package com.innovation_matrix.careroute.sse;

import com.innovation_matrix.careroute.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal AuthUser user) {
        return sseService.subscribe(user.getId());
    }
}
