package com.innovation_matrix.careroute.followup;

import java.util.List;

import com.innovation_matrix.careroute.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow-ups")
@PreAuthorize("hasRole('ASHA')")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping("/due")
    public List<FollowUpDto> dueList(@AuthenticationPrincipal AuthUser user) {
        return followUpService.dueList(user);
    }

    @PostMapping("/{id}/complete")
    public FollowUpDto complete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                @Valid @RequestBody(required = false) CompleteRequest body) {
        return followUpService.complete(user, id, body != null ? body.note() : null);
    }

    public record CompleteRequest(@Size(max = 500) String note) {
    }
}
