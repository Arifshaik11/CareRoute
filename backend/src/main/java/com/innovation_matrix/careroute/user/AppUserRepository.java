package com.innovation_matrix.careroute.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByRoleAndActiveTrue(Role role);
}
