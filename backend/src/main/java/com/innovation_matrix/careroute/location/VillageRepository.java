package com.innovation_matrix.careroute.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VillageRepository extends JpaRepository<Village, Long> {

    List<Village> findAllByOrderByNameAsc();

    List<Village> findByFacilityId(Long facilityId);
}
