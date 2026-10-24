package com.innovation_matrix.careroute.audit;

import com.innovation_matrix.careroute.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "actor_id")
    private Long actorId;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 40)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "text")
    private String details;

    protected AuditLog() {
    }

    public AuditLog(Long actorId, String action, String entityType, Long entityId, String details) {
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }
}
