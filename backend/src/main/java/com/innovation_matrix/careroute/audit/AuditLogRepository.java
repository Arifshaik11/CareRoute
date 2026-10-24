package com.innovation_matrix.careroute.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop50ByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
