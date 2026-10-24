package com.innovation_matrix.careroute.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records who accessed or changed which record — the accountability half of privacy. */
@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final AppUserRepository users;

    public AuditService(AuditLogRepository repository, AppUserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @Transactional
    public void record(Long actorId, String action, String entityType, Long entityId, String details) {
        repository.save(new AuditLog(actorId, action, entityType, entityId, details));
    }

    @Transactional(readOnly = true)
    public List<AuditEntryDto> history(String entityType, Long entityId) {
        List<AuditLog> entries = repository.findTop50ByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        Map<Long, AppUser> actors = users.findAllById(
                        entries.stream().map(AuditLog::getActorId).filter(id -> id != null).distinct().toList())
                .stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));
        return entries.stream()
                .map(e -> {
                    AppUser actor = actors.get(e.getActorId());
                    return new AuditEntryDto(
                            e.getAction(),
                            actor != null ? actor.getFullName() : "System",
                            actor != null ? actor.getRole().name() : null,
                            e.getDetails(),
                            e.getCreatedAt());
                })
                .toList();
    }

    public record AuditEntryDto(String action, String actorName, String actorRole, String details, Instant at) {
    }
}
