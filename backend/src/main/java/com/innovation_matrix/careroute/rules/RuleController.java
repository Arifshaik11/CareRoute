package com.innovation_matrix.careroute.rules;

import java.util.List;

import com.innovation_matrix.careroute.rules.dto.RuleDto;
import com.innovation_matrix.careroute.rules.dto.RuleRequest;
import com.innovation_matrix.careroute.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rules are readable by every role (transparency); only supervisors may change them. */
@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public List<RuleDto> list() {
        return ruleService.list();
    }

    @GetMapping("/{id}")
    public RuleDto get(@PathVariable Long id) {
        return ruleService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPERVISOR')")
    public RuleDto create(@Valid @RequestBody RuleRequest request, @AuthenticationPrincipal AuthUser user) {
        return ruleService.create(request, user.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public RuleDto update(@PathVariable Long id, @Valid @RequestBody RuleRequest request,
                          @AuthenticationPrincipal AuthUser user) {
        return ruleService.update(id, request, user.getId());
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public RuleDto setActive(@PathVariable Long id, @RequestBody ActiveRequest request,
                             @AuthenticationPrincipal AuthUser user) {
        return ruleService.setActive(id, request.active(), user.getId());
    }

    public record ActiveRequest(boolean active) {
    }
}
