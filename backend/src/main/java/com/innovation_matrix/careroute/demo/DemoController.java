package com.innovation_matrix.careroute.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Only exists when app.demo.enabled=true. Never enable on real data. */
@RestController
@RequestMapping("/api/demo")
@PreAuthorize("hasRole('SUPERVISOR')")
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/fast-forward")
    public DemoService.FastForwardResult fastForward(@Valid @RequestBody FastForwardRequest request) {
        return demoService.fastForward(request.hours());
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        demoService.reset();
        return ResponseEntity.noContent().build();
    }

    public record FastForwardRequest(@Min(1) @Max(720) int hours) {
    }
}
