package com.innovation_matrix.careroute.followup;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.innovation_matrix.careroute.audit.AuditService;
import com.innovation_matrix.careroute.common.error.NotFoundException;
import com.innovation_matrix.careroute.security.AuthUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowUpService {

    /** Most urgent first; within the same urgency, earliest due first. */
    static final Comparator<FollowUp> DUE_LIST_ORDER = Comparator
            .comparing(FollowUp::getPriority, Comparator.reverseOrder())
            .thenComparing(FollowUp::getDueAt);

    private final FollowUpRepository repository;
    private final AuditService audit;
    private final Clock clock;

    public FollowUpService(FollowUpRepository repository, AuditService audit, Clock clock) {
        this.repository = repository;
        this.audit = audit;
        this.clock = clock;
    }

    /** The ASHA's prioritised list of pending tasks — the single list that replaces the registers. */
    @Transactional(readOnly = true)
    public List<FollowUpDto> dueList(AuthUser user) {
        Instant now = clock.instant();
        return repository.findForAssignee(user.getId(), FollowUpStatus.PENDING).stream()
                .sorted(DUE_LIST_ORDER)
                .map(f -> FollowUpDto.from(f, now))
                .toList();
    }

    @Transactional
    public FollowUpDto complete(AuthUser user, Long id, String note) {
        FollowUp followUp = repository.findById(id).orElseThrow(() -> new NotFoundException("Follow-up", id));
        if (!Objects.equals(followUp.getAssignedTo().getId(), user.getId())) {
            throw new AccessDeniedException("Only the assigned ASHA can complete this follow-up");
        }
        Instant now = clock.instant();
        followUp.complete(now, note == null || note.isBlank() ? null : note.trim());
        audit.record(user.getId(), "FOLLOW_UP_COMPLETED", "PATIENT", followUp.getPatient().getId(),
                "Follow-up #" + id);
        return FollowUpDto.from(followUp, now);
    }
}
