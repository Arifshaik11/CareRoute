package com.innovation_matrix.careroute.patient;

import java.time.Instant;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.location.Village;
import com.innovation_matrix.careroute.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "patient")
public class Patient extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    /** The ASHA responsible for this patient. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asha_id", nullable = false)
    private AppUser asha;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Column(columnDefinition = "text")
    private String notes;

    protected Patient() {
    }

    public Patient(String fullName, int age, Sex sex, String phone, Program program,
                   Village village, AppUser asha, Instant consentAt, String notes) {
        this.fullName = fullName;
        this.age = age;
        this.sex = sex;
        this.phone = phone;
        this.program = program;
        this.village = village;
        this.asha = asha;
        this.consentGiven = true;
        this.consentAt = consentAt;
        this.notes = notes;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }

    public String getPhone() {
        return phone;
    }

    public Program getProgram() {
        return program;
    }

    public Village getVillage() {
        return village;
    }

    public AppUser getAsha() {
        return asha;
    }

    public boolean isConsentGiven() {
        return consentGiven;
    }

    public Instant getConsentAt() {
        return consentAt;
    }

    public String getNotes() {
        return notes;
    }
}
