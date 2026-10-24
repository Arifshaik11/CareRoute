package com.innovation_matrix.careroute.visit;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPatientIdOrderByVisitedAtDesc(Long patientId);

    long countByVisitedAtAfter(Instant since);
}
