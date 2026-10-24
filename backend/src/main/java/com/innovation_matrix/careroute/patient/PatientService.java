package com.innovation_matrix.careroute.patient;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.innovation_matrix.careroute.audit.AuditService;
import com.innovation_matrix.careroute.careplan.CarePlanService;
import com.innovation_matrix.careroute.careplan.CarePlanService.CarePlanResult;
import com.innovation_matrix.careroute.careplan.VisitInput;
import com.innovation_matrix.careroute.common.error.NotFoundException;
import com.innovation_matrix.careroute.common.error.ValidationException;
import com.innovation_matrix.careroute.followup.FollowUpDto;
import com.innovation_matrix.careroute.followup.FollowUpRepository;
import com.innovation_matrix.careroute.location.Village;
import com.innovation_matrix.careroute.location.VillageRepository;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.AuditHistoryDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.CreatePatientRequest;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.PatientDetailDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.PatientSummaryDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.RecordVisitRequest;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.VisitDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.VisitResultDto;
import com.innovation_matrix.careroute.referral.ReferralRepository;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;
import com.innovation_matrix.careroute.security.AccessPolicy;
import com.innovation_matrix.careroute.security.AuthUser;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.AppUserRepository;
import com.innovation_matrix.careroute.visit.Symptom;
import com.innovation_matrix.careroute.visit.VisitRepository;
import com.innovation_matrix.careroute.visit.VisitSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patients;
    private final VisitRepository visits;
    private final FollowUpRepository followUps;
    private final ReferralRepository referrals;
    private final VillageRepository villages;
    private final AppUserRepository users;
    private final CarePlanService carePlan;
    private final AccessPolicy access;
    private final AuditService audit;
    private final Clock clock;

    public PatientService(PatientRepository patients, VisitRepository visits, FollowUpRepository followUps,
                          ReferralRepository referrals, VillageRepository villages, AppUserRepository users,
                          CarePlanService carePlan, AccessPolicy access, AuditService audit, Clock clock) {
        this.patients = patients;
        this.visits = visits;
        this.followUps = followUps;
        this.referrals = referrals;
        this.villages = villages;
        this.users = users;
        this.carePlan = carePlan;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PatientSummaryDto> search(AuthUser user, Program program, String q) {
        var spec = switch (user.getRole()) {
            case ASHA -> PatientSpecifications.matching(user.getId(), null, program, q);
            case PHC_STAFF -> PatientSpecifications.matching(null,
                    villages.findByFacilityId(user.getFacilityId()).stream().map(Village::getId).toList(),
                    program, q);
            case SUPERVISOR -> PatientSpecifications.matching(null, null, program, q);
        };
        return patients.findAll(spec, Sort.by("fullName")).stream()
                .map(p -> PatientSummaryDto.from(p, user.getRole()))
                .toList();
    }

    @Transactional
    public PatientDetailDto get(AuthUser user, Long id) {
        Patient patient = find(id);
        access.checkCanView(user, patient);
        audit.record(user.getId(), "PATIENT_VIEWED", "PATIENT", id, null);
        Instant now = clock.instant();
        return new PatientDetailDto(
                PatientSummaryDto.from(patient, user.getRole()),
                patient.isConsentGiven(),
                patient.getConsentAt(),
                patient.getNotes(),
                visits.findByPatientIdOrderByVisitedAtDesc(id).stream().map(VisitDto::from).toList(),
                followUps.findByPatient(id).stream().map(f -> FollowUpDto.from(f, now)).toList(),
                referrals.findForPatient(id).stream().map(r -> ReferralDto.detail(r, now)).toList());
    }

    @Transactional
    public PatientSummaryDto register(AuthUser user, CreatePatientRequest request) {
        AppUser asha = users.findById(user.getId()).orElseThrow(() -> new NotFoundException("User", user.getId()));
        if (asha.getVillage() == null) {
            throw new ValidationException("Your account is not linked to a village");
        }
        Patient patient = patients.save(new Patient(
                request.fullName().trim(),
                request.age(),
                request.sex(),
                request.phone() == null || request.phone().isBlank() ? null : request.phone(),
                request.program(),
                asha.getVillage(),
                asha,
                clock.instant(),
                request.notes() == null || request.notes().isBlank() ? null : request.notes().trim()));
        audit.record(user.getId(), "PATIENT_REGISTERED", "PATIENT", patient.getId(), "Consent recorded");
        return PatientSummaryDto.from(patient, user.getRole());
    }

    @Transactional
    public VisitResultDto recordVisit(AuthUser user, Long patientId, RecordVisitRequest request) {
        Patient patient = find(patientId);
        access.checkCanRecordVisit(user, patient);
        validateVisit(patient, request);

        AppUser asha = users.getReferenceById(user.getId());
        VisitInput input = new VisitInput(
                request.systolicBp(),
                request.diastolicBp(),
                request.weightKg(),
                request.temperatureC(),
                request.pregnancyWeeks(),
                request.doseTaken(),
                request.symptoms() == null ? List.of()
                        : request.symptoms().stream().distinct().map(Symptom::name).toList(),
                request.notes() == null || request.notes().isBlank() ? null : request.notes().trim(),
                request.source() != null ? request.source() : VisitSource.MANUAL);

        CarePlanResult result = carePlan.applyVisit(patient, asha, input, clock.instant());
        audit.record(user.getId(), "VISIT_RECORDED", "PATIENT", patientId,
                result.outcomes().size() + " rule(s) fired");
        return new VisitResultDto(VisitDto.from(result.visit()), result.outcomes(), result.fulfilledFollowUps());
    }

    @Transactional(readOnly = true)
    public AuditHistoryDto accessHistory(Long patientId) {
        find(patientId);
        return new AuditHistoryDto(audit.history("PATIENT", patientId));
    }

    private static void validateVisit(Patient patient, RecordVisitRequest r) {
        if ((r.systolicBp() == null) != (r.diastolicBp() == null)) {
            throw new ValidationException("Record both systolic and diastolic BP, or neither");
        }
        if (r.systolicBp() != null && r.systolicBp() <= r.diastolicBp()) {
            throw new ValidationException("Systolic BP must be higher than diastolic BP");
        }
        if (r.pregnancyWeeks() != null && patient.getProgram() != Program.MATERNAL) {
            throw new ValidationException("Pregnancy weeks apply only to maternal-care patients");
        }
    }

    private Patient find(Long id) {
        return patients.findById(id).orElseThrow(() -> new NotFoundException("Patient", id));
    }
}
