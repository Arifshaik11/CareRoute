package com.innovation_matrix.careroute.followup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    @Query("""
            select f from FollowUp f
              join fetch f.patient p
              join fetch p.village
              left join fetch f.rule
            where f.assignedTo.id = :userId and f.status = :status
            """)
    List<FollowUp> findForAssignee(@Param("userId") Long userId, @Param("status") FollowUpStatus status);

    @Query("""
            select f from FollowUp f
              left join fetch f.rule
            where f.patient.id = :patientId
            order by f.dueAt desc
            """)
    List<FollowUp> findByPatient(@Param("patientId") Long patientId);

    List<FollowUp> findByPatientIdAndStatus(Long patientId, FollowUpStatus status);

    List<FollowUp> findByStatus(FollowUpStatus status);

    @Query("""
            select f from FollowUp f
              join fetch f.patient p
              join fetch p.village
            where f.status = :status
            """)
    List<FollowUp> findByStatusWithVillage(@Param("status") FollowUpStatus status);
}
