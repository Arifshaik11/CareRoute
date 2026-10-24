package com.innovation_matrix.careroute.referral;

import java.util.List;

import com.innovation_matrix.careroute.referral.dto.ReferralActionRequest;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;
import com.innovation_matrix.careroute.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    /** Scoped by role: ASHA sees her patients', PHC staff their facility's, supervisors all. */
    @GetMapping
    public List<ReferralDto> list(@AuthenticationPrincipal AuthUser user,
                                  @RequestParam(required = false) ReferralStatus status,
                                  @RequestParam(defaultValue = "false") boolean escalated) {
        return referralService.list(user, status, escalated);
    }

    @GetMapping("/{id}")
    public ReferralDto get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return referralService.get(user, id);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('PHC_STAFF')")
    public ReferralDto acknowledge(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @Valid @RequestBody(required = false) ReferralActionRequest body) {
        return referralService.acknowledge(user, id, body != null ? body.note() : null);
    }

    @PostMapping("/{id}/arrive")
    @PreAuthorize("hasRole('PHC_STAFF')")
    public ReferralDto arrive(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                              @Valid @RequestBody(required = false) ReferralActionRequest body) {
        return referralService.markArrived(user, id, body != null ? body.note() : null);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('PHC_STAFF')")
    public ReferralDto close(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                             @Valid @RequestBody ReferralActionRequest body) {
        return referralService.close(user, id, body.note());
    }
}
