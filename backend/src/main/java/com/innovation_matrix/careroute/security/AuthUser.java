package com.innovation_matrix.careroute.security;

import java.util.Collection;
import java.util.List;

import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Immutable authenticated principal, detached from JPA so it is safe to keep per request. */
public final class AuthUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final Long villageId;
    private final Long facilityId;
    private final boolean active;

    private AuthUser(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.villageId = user.getVillage() != null ? user.getVillage().getId() : null;
        this.facilityId = user.getFacility() != null ? user.getFacility().getId() : null;
        this.active = user.isActive();
    }

    public static AuthUser from(AppUser user) {
        return new AuthUser(user);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public Long getVillageId() {
        return villageId;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public boolean hasRole(Role r) {
        return role == r;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
