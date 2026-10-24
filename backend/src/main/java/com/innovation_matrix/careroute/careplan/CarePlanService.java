package com.innovation_matrix.careroute.careplan;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.innovation_matrix.careroute.followup.FollowUp;
import com.innovation_matrix.careroute.followup.FollowUpRepository;
import com.innovation_matrix.careroute.followup.FollowUpStatus;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.referral.ReferralRepository;
import com.innovation_matrix.careroute.referral.ReferralService;
import com.innovation_matrix.careroute.rules.CareRuleRepository;
import com.innovation_matrix.careroute.rules.RuleAction;
import com.innovation_matrix.careroute.rules.RuleEvaluator;
import com.innovation_matrix.careroute.rules.RuleMatch;
import com.innovation_matrix.careroute.rules.VisitFacts;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.visit.Visit;
import com.innovation_matrix.careroute.visit.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The heart of CareRoute: turns a visit's observations into the next steps.
 * <ol>
 *   <li>Save the visit.</li>
 *   <li>The visit fulfils the patient's pending follow-ups.</li>
 *   <li>Run the active rules; each FOLLOW_UP rule adds a task to the ASHA's due list.</li>
 *   <li>The most urgent REFERRAL rule creates one referral, unless one is already open.</li>
 * </ol>
 */
@Service
public class CarePlanService {

    /** Most urgent first; for equal urgency, the shorter deadline wins. */
    private static final Comparator<RuleMatch> REFERRAL_ORDER = Comparator
            .comparing(RuleMatch::priority, Comparator.reverseOrder())
            .thenComparingInt(RuleMatch::dueWithinHours);

    private final VisitRepository visits;
    private final FollowUpRepository followUps;
    private final ReferralRepository referrals;
    private final CareRuleRepository rules;
    private final RuleEvaluator evaluator;

    public CarePlanService(VisitRepository visits, FollowUpRepository followUps, ReferralRepository referrals,
                           CareRuleRepository rules, RuleEvaluator evaluator) {
        this.visits = visits;
        this.followUps = followUps;
        this.referrals = referrals;
        this.rules = rules;
        this.evaluator = evaluator;
    }

    @Transactional
    public CarePlanResult applyVisit(Patient patient, AppUser recordedBy, VisitInput input, Instant visitedAt) {
        Visit visit = visits.save(toVisit(patient, recordedBy, input, visitedAt));

        List<FollowUp> fulfilled = followUps.findByPatientIdAndStatus(patient.getId(), FollowUpStatus.PENDING);
        fulfilled.forEach(f -> f.complete(visitedAt, "Completed by home visit"));

        VisitFacts facts = toFacts(patient, visit);
        List<RuleMatch> matches = evaluator.evaluate(rules.findByActiveTrue(), facts);

        List<RuleOutcome> outcomes = new ArrayList<>();
        matches.stream()
                .filter(m -> m.action() == RuleAction.FOLLOW_UP)
                .forEach(m -> outcomes.add(createFollowUp(patient, visit, m, visitedAt)));
        outcomes.addAll(createReferral(patient, recordedBy, visit, matches, visitedAt));

        return new CarePlanResult(visit, outcomes, fulfilled.size());
    }

    private RuleOutcome createFollowUp(Patient patient, Visit visit, RuleMatch m, Instant visitedAt) {
        Instant dueAt = visitedAt.plus(Duration.ofHours(m.dueWithinHours()));
        FollowUp task = new FollowUp(patient, patient.getAsha(), m.explanation(), m.priority(), dueAt, visitedAt);
        task.setVisit(visit);
        task.setRule(m.rule());
        followUps.save(task);
        return outcome(m, RuleOutcome.Result.FOLLOW_UP_CREATED, dueAt, task.getId(), null);
    }

    private List<RuleOutcome> createReferral(Patient patient, AppUser recordedBy, Visit visit,
                                             List<RuleMatch> matches, Instant visitedAt) {
        List<RuleMatch> referralMatches = matches.stream()
                .filter(m -> m.action() == RuleAction.REFERRAL)
                .sorted(REFERRAL_ORDER)
                .toList();
        if (referralMatches.isEmpty()) {
            return List.of();
        }
        List<RuleOutcome> outcomes = new ArrayList<>();
        if (referrals.existsByPatientIdAndStatusIn(patient.getId(), ReferralService.OPEN_STATUSES)) {
            referralMatches.forEach(m -> outcomes.add(outcome(m, RuleOutcome.Result.ALREADY_REFERRED, null, null, null)));
            return outcomes;
        }

        RuleMatch primary = referralMatches.get(0);
        Instant dueAt = visitedAt.plus(Duration.ofHours(primary.dueWithinHours()));
        Referral referral = referrals.save(Referral.create(
                patient, visit, primary.rule(), patient.getVillage().getFacility(), recordedBy,
                primary.explanation(), primary.priority(), visitedAt, dueAt));
        outcomes.add(outcome(primary, RuleOutcome.Result.REFERRAL_CREATED, dueAt, null, referral.getId()));

        var seen = new HashSet<Long>();
        seen.add(primary.rule().getId());
        referralMatches.stream().skip(1)
                .filter(m -> seen.add(m.rule().getId()))
                .forEach(m -> outcomes.add(outcome(m, RuleOutcome.Result.COVERED_BY_REFERRAL, dueAt, null,
                        referral.getId())));
        return outcomes;
    }

    private static RuleOutcome outcome(RuleMatch m, RuleOutcome.Result result, Instant dueAt,
                                       Long followUpId, Long referralId) {
        return new RuleOutcome(m.rule().getCode(), m.rule().getName(), m.action(), m.priority(),
                m.explanation(), result, dueAt, followUpId, referralId);
    }

    private static Visit toVisit(Patient patient, AppUser recordedBy, VisitInput in, Instant visitedAt) {
        Visit v = new Visit(patient, recordedBy, visitedAt, in.source());
        v.setSystolicBp(in.systolicBp());
        v.setDiastolicBp(in.diastolicBp());
        v.setWeightKg(in.weightKg());
        v.setTemperatureC(in.temperatureC());
        v.setPregnancyWeeks(in.pregnancyWeeks());
        v.setDoseTaken(in.doseTaken());
        v.setSymptoms(in.symptoms());
        v.setNotes(in.notes());
        return v;
    }

    private static VisitFacts toFacts(Patient patient, Visit v) {
        return new VisitFacts(
                patient.getProgram(),
                patient.getAge(),
                v.getSystolicBp(),
                v.getDiastolicBp(),
                v.getTemperatureC(),
                v.getWeightKg(),
                v.getPregnancyWeeks(),
                v.getDoseTaken(),
                new HashSet<>(v.getSymptoms()));
    }

    public record CarePlanResult(Visit visit, List<RuleOutcome> outcomes, int fulfilledFollowUps) {
    }
}
