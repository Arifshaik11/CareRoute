package com.innovation_matrix.careroute.user;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.location.Facility;
import com.innovation_matrix.careroute.location.Village;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 20)
    private String phone;

    /** Set for ASHA workers. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id")
    private Village village;

    /** Set for facility staff. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(nullable = false)
    private boolean active = true;

    protected AppUser() {
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public Village getVillage() {
        return village;
    }

    public Facility getFacility() {
        return facility;
    }

    public boolean isActive() {
        return active;
    }
}
