package com.innovation_matrix.careroute.patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/** Composable patient filters; null arguments are ignored. */
public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> matching(Long ashaId, Collection<Long> villageIds, Program program, String q) {
        return (root, query, cb) -> {
            boolean isCountQuery = query != null
                    && (query.getResultType() == Long.class || query.getResultType() == long.class);
            if (!isCountQuery) {
                root.fetch("village");
                root.fetch("asha");
            }
            List<Predicate> predicates = new ArrayList<>();
            if (ashaId != null) {
                predicates.add(cb.equal(root.get("asha").get("id"), ashaId));
            }
            if (villageIds != null) {
                predicates.add(villageIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("village").get("id").in(villageIds));
            }
            if (program != null) {
                predicates.add(cb.equal(root.get("program"), program));
            }
            if (q != null && !q.isBlank()) {
                predicates.add(cb.like(cb.lower(root.<String>get("fullName")), "%" + q.trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
