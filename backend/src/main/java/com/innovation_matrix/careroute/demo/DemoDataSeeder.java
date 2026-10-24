package com.innovation_matrix.careroute.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.innovation_matrix.careroute.careplan.CarePlanService;
import com.innovation_matrix.careroute.careplan.VisitInput;
import com.innovation_matrix.careroute.location.Village;
import com.innovation_matrix.careroute.location.VillageRepository;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.patient.PatientRepository;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.patient.Sex;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.referral.ReferralRepository;
import com.innovation_matrix.careroute.referral.ReferralService;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.AppUserRepository;
import com.innovation_matrix.careroute.user.Role;
import com.innovation_matrix.careroute.visit.VisitSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates a realistic, fully synthetic block: ~16 patients per village with 6–8 weeks of
 * visit history. Every visit goes through the real CarePlanService, so follow-ups, referrals
 * and escalations are produced by the same rules the live app uses. Deterministic (fixed seed).
 * <p>
 * Includes "Lakshmi Devi", the patient used in the live demo story.
 */
@Component
public class DemoDataSeeder {

    public static final String DEMO_PATIENT_NAME = "Lakshmi Devi";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final int PATIENTS_PER_VILLAGE = 16;

    private static final String[] FEMALE_NAMES = {"Anitha", "Bhavani", "Chandrika", "Deepika", "Durga", "Geetha",
            "Haritha", "Jyothi", "Kavitha", "Lavanya", "Madhavi", "Manjula", "Nandini", "Padmaja", "Pavani", "Radha",
            "Rajeswari", "Saritha", "Sirisha", "Swathi", "Tulasi", "Usha", "Vani", "Vijaya"};
    private static final String[] MALE_NAMES = {"Anil", "Bhaskar", "Chandra", "Dinesh", "Ganesh", "Hari", "Kishore",
            "Krishna", "Mahesh", "Nagaraju", "Prasad", "Raju", "Ramesh", "Ravi", "Srinivas", "Suresh", "Venkat", "Venu"};
    private static final String[] INITIALS = {"B", "C", "G", "K", "M", "N", "P", "R", "S", "T", "V", "Y"};

    private final PatientRepository patients;
    private final VillageRepository villages;
    private final AppUserRepository users;
    private final ReferralRepository referrals;
    private final CarePlanService carePlan;
    private final ReferralService referralService;
    private final Clock clock;

    public DemoDataSeeder(PatientRepository patients, VillageRepository villages, AppUserRepository users,
                          ReferralRepository referrals, CarePlanService carePlan, ReferralService referralService,
                          Clock clock) {
        this.patients = patients;
        this.villages = villages;
        this.users = users;
        this.referrals = referrals;
        this.carePlan = carePlan;
        this.referralService = referralService;
        this.clock = clock;
    }

    @Transactional
    public void seed() {
        Instant now = clock.instant();
        Random rnd = new Random(2026);

        Map<Long, AppUser> ashaByVillage = users.findByRoleAndActiveTrue(Role.ASHA).stream()
                .filter(u -> u.getVillage() != null)
                .collect(Collectors.toMap(u -> u.getVillage().getId(), Function.identity(), (a, b) -> a));
        AppUser facilityStaff = users.findByRoleAndActiveTrue(Role.PHC_STAFF).stream().findFirst().orElse(null);

        int patientCount = 0;
        for (Village village : villages.findAllByOrderByNameAsc()) {
            AppUser asha = ashaByVillage.get(village.getId());
            if (asha == null) {
                continue;
            }
            for (int i = 0; i < PATIENTS_PER_VILLAGE; i++) {
                seedPatientWithHistory(village, asha, rnd, now);
                patientCount++;
            }
            if ("Ramapuram".equals(village.getName())) {
                seedDemoPatient(village, asha, now);
                patientCount++;
            }
        }

        if (facilityStaff != null) {
            progressReferrals(facilityStaff, rnd, now);
        }
        int escalated = referralService.escalateOverdue(now);
        log.info("Seeded {} synthetic patients; {} referral(s) escalated", patientCount, escalated);
    }

    // ------------------------------------------------------------------ patients

    private void seedPatientWithHistory(Village village, AppUser asha, Random rnd, Instant now) {
        Program program = pickProgram(rnd);
        Sex sex = program == Program.MATERNAL ? Sex.FEMALE : (rnd.nextBoolean() ? Sex.FEMALE : Sex.MALE);
        int age = switch (program) {
            case MATERNAL -> 19 + rnd.nextInt(16);
            case TB -> 18 + rnd.nextInt(50);
            case CHILD_IMMUNIZATION -> rnd.nextInt(6);
            case NCD -> 40 + rnd.nextInt(35);
        };
        String name = (sex == Sex.FEMALE ? pick(FEMALE_NAMES, rnd) : pick(MALE_NAMES, rnd)) + " " + pick(INITIALS, rnd);
        Instant registeredAt = now.minus(Duration.ofDays(42 + rnd.nextInt(28))).minus(Duration.ofHours(rnd.nextInt(10)));

        Patient patient = new Patient(name, age, sex, syntheticPhone(rnd), program, village, asha, registeredAt, null);
        patient.setCreatedAt(registeredAt);
        patients.save(patient);

        int pregnancyWeeksAtRegistration = 14 + rnd.nextInt(14);
        Instant visitAt = registeredAt.plus(Duration.ofHours(2));
        int visitCount = 1 + rnd.nextInt(3);
        for (int v = 0; v < visitCount && visitAt.isBefore(now.minus(Duration.ofHours(6))); v++) {
            int weeks = pregnancyWeeksAtRegistration
                    + (int) Duration.between(registeredAt, visitAt).toDays() / 7;
            carePlan.applyVisit(patient, asha, observations(program, weeks, rnd), visitAt);
            visitAt = visitAt.plus(Duration.ofDays(9 + rnd.nextInt(12))).plus(Duration.ofHours(rnd.nextInt(8)));
        }
    }

    /**
     * Lakshmi: third trimester, last visit two weeks ago with normal readings, so her routine
     * home visit is due in a few hours. In the demo the ASHA records a raised BP and the loop starts.
     */
    private void seedDemoPatient(Village village, AppUser asha, Instant now) {
        Instant registeredAt = now.minus(Duration.ofDays(45));
        Patient lakshmi = new Patient(DEMO_PATIENT_NAME, 24, Sex.FEMALE, "9848000123", Program.MATERNAL,
                village, asha, registeredAt, "First pregnancy. Synthetic demo record.");
        lakshmi.setCreatedAt(registeredAt);
        patients.save(lakshmi);

        Instant firstVisit = registeredAt.plus(Duration.ofHours(3));
        carePlan.applyVisit(lakshmi, asha, new VisitInput(112, 72, decimal(51.0, 1), decimal(36.7, 1), 22,
                null, List.of(), "Registered at home visit", VisitSource.MANUAL), firstVisit);

        Instant lastVisit = now.minus(Duration.ofHours(336 - 4));   // routine follow-up due in ~4 hours
        carePlan.applyVisit(lakshmi, asha, new VisitInput(118, 76, decimal(54.5, 1), decimal(36.8, 1), 28,
                null, List.of(), "Feeling well", VisitSource.MANUAL), lastVisit);
    }

    private static VisitInput observations(Program program, int pregnancyWeeks, Random rnd) {
        List<String> symptoms = new ArrayList<>();
        return switch (program) {
            case MATERNAL -> {
                int[] bp = maternalBp(rnd);
                if (rnd.nextDouble() < 0.06) {
                    symptoms.add(rnd.nextBoolean() ? "SEVERE_HEADACHE" : "SWELLING");
                }
                yield new VisitInput(bp[0], bp[1], decimal(46 + rnd.nextDouble() * 20, 1),
                        decimal(36.4 + rnd.nextDouble() * 0.9, 1), Math.min(pregnancyWeeks, 40), null,
                        symptoms, null, VisitSource.MANUAL);
            }
            case TB -> {
                boolean doseTaken = rnd.nextDouble() > 0.2;
                if (rnd.nextDouble() < 0.5) {
                    symptoms.add("COUGH_2_WEEKS");
                }
                if (rnd.nextDouble() < 0.07) {
                    symptoms.add("HEMOPTYSIS");
                }
                double temp = rnd.nextDouble() < 0.07 ? 39.1 + rnd.nextDouble() * 0.6 : 36.5 + rnd.nextDouble() * 1.2;
                yield new VisitInput(null, null, decimal(42 + rnd.nextDouble() * 25, 1), decimal(temp, 1), null,
                        doseTaken, symptoms, null, VisitSource.MANUAL);
            }
            case CHILD_IMMUNIZATION -> {
                double roll = rnd.nextDouble();
                double temp = roll < 0.05 ? 39.2 + rnd.nextDouble() * 0.5
                        : roll < 0.18 ? 38.1 + rnd.nextDouble() * 0.7
                        : 36.5 + rnd.nextDouble();
                if (temp >= 38.0) {
                    symptoms.add("FEVER");
                }
                yield new VisitInput(null, null, decimal(4 + rnd.nextDouble() * 14, 1), decimal(temp, 1), null,
                        null, symptoms, null, VisitSource.MANUAL);
            }
            case NCD -> {
                boolean high = rnd.nextDouble() < 0.18;
                int systolic = high ? 162 + rnd.nextInt(14) : 118 + rnd.nextInt(34);
                int diastolic = high ? 100 + rnd.nextInt(9) : 74 + rnd.nextInt(20);
                yield new VisitInput(systolic, diastolic, decimal(50 + rnd.nextDouble() * 30, 1),
                        decimal(36.5 + rnd.nextDouble() * 0.8, 1), null, null, symptoms, null, VisitSource.MANUAL);
            }
        };
    }

    private static int[] maternalBp(Random rnd) {
        double roll = rnd.nextDouble();
        if (roll < 0.04) {
            return new int[]{162 + rnd.nextInt(8), 110 + rnd.nextInt(5)};
        }
        if (roll < 0.22) {
            return new int[]{141 + rnd.nextInt(16), 90 + rnd.nextInt(10)};
        }
        return new int[]{104 + rnd.nextInt(28), 64 + rnd.nextInt(20)};
    }

    // ------------------------------------------------------------------ referrals

    /** Facility actions on historical referrals, so the funnel shows a realistic picture. */
    private void progressReferrals(AppUser staff, Random rnd, Instant now) {
        for (Referral referral : referrals.findByStatusIn(ReferralService.OPEN_STATUSES)) {
            Instant created = referral.getCreatedAt();
            double roll = rnd.nextDouble();
            Instant ackAt = created.plus(Duration.ofHours(2 + rnd.nextInt(8)));
            Instant arrivedAt = ackAt.plus(Duration.ofHours(4 + rnd.nextInt(30)));
            Instant closedAt = arrivedAt.plus(Duration.ofHours(1 + rnd.nextInt(5)));

            if (roll < 0.62 && arrivedAt.isBefore(now)) {
                referral.acknowledge(staff, null, ackAt);
                referral.markArrived(staff, "Patient seen at OPD", arrivedAt);
                if (rnd.nextDouble() < 0.7 && closedAt.isBefore(now)) {
                    referral.close(staff, "Reviewed by medical officer; care plan updated", closedAt);
                }
            } else if (roll < 0.80 && ackAt.isBefore(now)) {
                referral.acknowledge(staff, "Expecting patient", ackAt);
            }
            // Remaining referrals stay untouched: the "leaks" the dashboard surfaces.
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Program pickProgram(Random rnd) {
        double roll = rnd.nextDouble();
        if (roll < 0.40) {
            return Program.MATERNAL;
        }
        if (roll < 0.60) {
            return Program.TB;
        }
        if (roll < 0.80) {
            return Program.CHILD_IMMUNIZATION;
        }
        return Program.NCD;
    }

    private static String pick(String[] values, Random rnd) {
        return values[rnd.nextInt(values.length)];
    }

    private static String syntheticPhone(Random rnd) {
        return "9" + String.format("%09d", rnd.nextInt(1_000_000_000));
    }

    private static BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }
}
