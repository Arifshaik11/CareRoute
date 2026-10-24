package com.innovation_matrix.careroute.referral;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferralRepository extends JpaRepository<Referral, Long> {

    String LIST_FETCH = """
            select r from Referral r
              join fetch r.patient p
              join fetch p.village
              join fetch p.asha
              join fetch r.facility
              left join fetch r.rule
            """;

    @Query(LIST_FETCH + " order by r.dueAt asc")
    List<Referral> findAllForList();

    @Query(LIST_FETCH + " where r.facility.id = :facilityId order by r.dueAt asc")
    List<Referral> findForFacility(@Param("facilityId") Long facilityId);

    @Query(LIST_FETCH + " where p.asha.id = :ashaId order by r.dueAt asc")
    List<Referral> findForAsha(@Param("ashaId") Long ashaId);

    @Query(LIST_FETCH + " where p.id = :patientId order by r.createdAt desc")
    List<Referral> findForPatient(@Param("patientId") Long patientId);

    boolean existsByPatientIdAndStatusIn(Long patientId, Collection<ReferralStatus> statuses);

    @Query("""
            select r from Referral r
            where r.status in :statuses and r.escalatedAt is null and r.dueAt < :now
            """)
    List<Referral> findOverdueNotEscalated(@Param("statuses") Collection<ReferralStatus> statuses,
                                           @Param("now") Instant now);

    List<Referral> findByStatusIn(Collection<ReferralStatus> statuses);
}
