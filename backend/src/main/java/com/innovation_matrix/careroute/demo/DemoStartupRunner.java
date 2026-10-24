package com.innovation_matrix.careroute.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Seeds synthetic patients on first start when demo mode is on. */
@Component
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
public class DemoStartupRunner implements ApplicationRunner {

    private final DemoService demoService;

    public DemoStartupRunner(DemoService demoService) {
        this.demoService = demoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoService.seedIfEmpty();
    }
}
