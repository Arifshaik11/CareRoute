package com.innovation_matrix.careroute.visit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Screening observations recorded at a home visit. Never a diagnosis. */
@Entity
@Table(name = "visit")
public class Visit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asha_id", nullable = false)
    private AppUser asha;

    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "temperature_c", precision = 4, scale = 1)
    private BigDecimal temperatureC;

    @Column(name = "pregnancy_weeks")
    private Integer pregnancyWeeks;

    @Column(name = "dose_taken")
    private Boolean doseTaken;

    /** Symptom codes (see {@link Symptom}) stored as a JSON array. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> symptoms = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VisitSource source = VisitSource.MANUAL;

    protected Visit() {
    }

    public Visit(Patient patient, AppUser asha, Instant visitedAt, VisitSource source) {
        this.patient = patient;
        this.asha = asha;
        this.visitedAt = visitedAt;
        this.source = source != null ? source : VisitSource.MANUAL;
        setCreatedAt(visitedAt);
    }

    public Patient getPatient() {
        return patient;
    }

    public AppUser getAsha() {
        return asha;
    }

    public Instant getVisitedAt() {
        return visitedAt;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(BigDecimal temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getPregnancyWeeks() {
        return pregnancyWeeks;
    }

    public void setPregnancyWeeks(Integer pregnancyWeeks) {
        this.pregnancyWeeks = pregnancyWeeks;
    }

    public Boolean getDoseTaken() {
        return doseTaken;
    }

    public void setDoseTaken(Boolean doseTaken) {
        this.doseTaken = doseTaken;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms != null ? new ArrayList<>(symptoms) : new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public VisitSource getSource() {
        return source;
    }
}
