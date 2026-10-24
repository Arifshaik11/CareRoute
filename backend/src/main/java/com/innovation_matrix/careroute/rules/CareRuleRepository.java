package com.innovation_matrix.careroute.rules;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CareRuleRepository extends JpaRepository<CareRule, Long> {

    List<CareRule> findByActiveTrue();

    List<CareRule> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
