package com.innovation_matrix.careroute.patient.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.innovation_matrix.careroute.audit.AuditService.AuditEntryDto;
import com.innovation_matrix.careroute.careplan.RuleOutcome;
import com.innovation_matrix.careroute.followup.FollowUpDto;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.patient.Sex;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;
import com.innovation_matrix.careroute.user.Role;
import com.innovation_matrix.careroute.visit.Symptom;
import com.innovation_matrix.careroute.visit.Visit;
import com.innovation_matrix.careroute.visit.VisitSource;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PatientDtos {

    private PatientDtos() {
    }

    // ---------- requests ----------

    public record CreatePatientRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotNull @Min(0) @Max(120) Integer age,
            @NotNull Sex sex,
            @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "must be a 10-digit Indian mobile number") String phone,
            @NotNull Program program,
            @AssertTrue(message = "the patient's consent is required to register them") boolean consentGiven,
            @Size(max = 1000) String notes) {
    }

    public record RecordVisitRequest(
            @Min(50) @Max(260) Integer systolicBp,
            @Min(30) @Max(180) Integer diastolicBp,
            @DecimalMin("0.5") @DecimalMax("300") BigDecimal weightKg,
            @DecimalMin("30.0") @DecimalMax("45.0") BigDecimal temperatureC,
            @Min(1) @Max(45) Integer pregnancyWeeks,
            Boolean doseTaken,
            @Size(max = 12) List<Symptom> symptoms,
            @Size(max = 1000) String notes,
            VisitSource source) {
    }

    // ---------- responses ----------

    public record PatientSummaryDto(
            Long id,
            String fullName,
            Integer age,
            Sex sex,
            Program program,
            String villageName,
            String ashaName,
            String phone,
            Instant registeredAt) {

        /** Call inside a transaction. Phone is masked for roles that do not need to call the patient. */
        public static PatientSummaryDto from(Patient p, Role viewerRole) {
            return new PatientSummaryDto(p.getId(), p.getFullName(), p.getAge(), p.getSex(), p.getProgram(),
                    p.getVillage().getName(), p.getAsha().getFullName(),
                    PhoneMasking.forRole(p.getPhone(), viewerRole), p.getCreatedAt());
        }
    }

    public record PatientDetailDto(
            PatientSummaryDto patient,
            boolean consentGiven,
            Instant consentAt,
            String notes,
            List<VisitDto> visits,
            List<FollowUpDto> followUps,
            List<ReferralDto> referrals) {
    }

    public record VisitDto(
            Long id,
            Instant visitedAt,
            String recordedBy,
            Integer systolicBp,
            Integer diastolicBp,
            BigDecimal weightKg,
            BigDecimal temperatureC,
            Integer pregnancyWeeks,
            Boolean doseTaken,
            List<String> symptoms,
            String notes,
            VisitSource source) {

        public static VisitDto from(Visit v) {
            return new VisitDto(v.getId(), v.getVisitedAt(), v.getAsha().getFullName(), v.getSystolicBp(),
                    v.getDiastolicBp(), v.getWeightKg(), v.getTemperatureC(), v.getPregnancyWeeks(),
                    v.getDoseTaken(), List.copyOf(v.getSymptoms()), v.getNotes(), v.getSource());
        }
    }

    /** Returned right after saving a visit: what the rules decided, and why. */
    public record VisitResultDto(VisitDto visit, List<RuleOutcome> outcomes, int completedFollowUps) {
    }

    public record AuditHistoryDto(List<AuditEntryDto> entries) {
    }
}
