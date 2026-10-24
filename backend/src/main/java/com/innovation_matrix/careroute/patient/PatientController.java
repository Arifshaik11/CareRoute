package com.innovation_matrix.careroute.patient;

import java.util.List;

import com.innovation_matrix.careroute.patient.dto.PatientDtos.AuditHistoryDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.CreatePatientRequest;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.PatientDetailDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.PatientSummaryDto;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.RecordVisitRequest;
import com.innovation_matrix.careroute.patient.dto.PatientDtos.VisitResultDto;
import com.innovation_matrix.careroute.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** Results are scoped to what the caller's role may see. */
    @GetMapping
    public List<PatientSummaryDto> search(@AuthenticationPrincipal AuthUser user,
                                          @RequestParam(required = false) Program program,
                                          @RequestParam(required = false) String q) {
        return patientService.search(user, program, q);
    }

    @GetMapping("/{id}")
    public PatientDetailDto get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return patientService.get(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ASHA')")
    public PatientSummaryDto register(@AuthenticationPrincipal AuthUser user,
                                      @Valid @RequestBody CreatePatientRequest request) {
        return patientService.register(user, request);
    }

    @PostMapping("/{id}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ASHA')")
    public VisitResultDto recordVisit(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                      @Valid @RequestBody RecordVisitRequest request) {
        return patientService.recordVisit(user, id, request);
    }

    /** Who has viewed or changed this patient's record. */
    @GetMapping("/{id}/access-log")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public AuditHistoryDto accessLog(@PathVariable Long id) {
        return patientService.accessHistory(id);
    }
}
