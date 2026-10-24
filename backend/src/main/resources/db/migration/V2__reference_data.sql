-- =====================================================================
-- Reference data: facilities, villages, demo users and starter rules.
-- All demo users share the password: demo@123
-- Synthetic patients are NOT seeded here; the app seeds them at startup
-- when app.demo.enabled=true (see DemoDataSeeder), so they can be reset.
-- =====================================================================

insert into facility (name, type, block)
values ('Ramapuram PHC', 'PHC', 'Ramapuram Block');

insert into village (name, block, district, facility_id)
values ('Ramapuram', 'Ramapuram Block', 'Demo District', (select id from facility where name = 'Ramapuram PHC')),
       ('Kondapalli', 'Ramapuram Block', 'Demo District', (select id from facility where name = 'Ramapuram PHC')),
       ('Gollapalli', 'Ramapuram Block', 'Demo District', (select id from facility where name = 'Ramapuram PHC'));

insert into app_user (username, password_hash, full_name, role, phone, village_id, facility_id)
values ('asha.sunitha', '$2a$10$T7U4YfxDTN4XHZCWGvImxeoRpFY0obnDw.Kd7tLYSD1rPJDpaQrEK', 'Sunitha K', 'ASHA',
        '9000000001', (select id from village where name = 'Ramapuram'), null),
       ('asha.padma', '$2a$10$T7U4YfxDTN4XHZCWGvImxeoRpFY0obnDw.Kd7tLYSD1rPJDpaQrEK', 'Padma R', 'ASHA',
        '9000000002', (select id from village where name = 'Kondapalli'), null),
       ('asha.rani', '$2a$10$T7U4YfxDTN4XHZCWGvImxeoRpFY0obnDw.Kd7tLYSD1rPJDpaQrEK', 'Rani M', 'ASHA',
        '9000000003', (select id from village where name = 'Gollapalli'), null),
       ('phc.kiran', '$2a$10$T7U4YfxDTN4XHZCWGvImxeoRpFY0obnDw.Kd7tLYSD1rPJDpaQrEK', 'Dr. Kiran V', 'PHC_STAFF',
        '9000000010', null, (select id from facility where name = 'Ramapuram PHC')),
       ('supervisor.anil', '$2a$10$T7U4YfxDTN4XHZCWGvImxeoRpFY0obnDw.Kd7tLYSD1rPJDpaQrEK', 'Anil P (Block Supervisor)',
        'SUPERVISOR', '9000000020', null, null);

-- Starter rules. Placeholders like {SYSTOLIC_BP} are filled with the observed value.
-- These are illustrative screening rules for a prototype, to be configured by the
-- programme's medical officer; they are not clinical guidance.
insert into care_rule (code, name, program, condition, action, priority, due_within_hours, explanation)
values ('MAT_SEVERE_BP', 'Pregnancy: severely raised BP', 'MATERNAL',
        '{"match":"ANY","clauses":[{"field":"SYSTOLIC_BP","op":"GTE","value":160},{"field":"DIASTOLIC_BP","op":"GTE","value":110}]}',
        'REFERRAL', 'CRITICAL', 12,
        'BP recorded as {SYSTOLIC_BP}/{DIASTOLIC_BP} mmHg in pregnancy (threshold 160/110). Refer to the PHC for review within 12 hours.'),

       ('MAT_HIGH_BP', 'Pregnancy: raised BP', 'MATERNAL',
        '{"match":"ANY","clauses":[{"field":"SYSTOLIC_BP","op":"GTE","value":140},{"field":"DIASTOLIC_BP","op":"GTE","value":90}]}',
        'REFERRAL', 'HIGH', 48,
        'BP recorded as {SYSTOLIC_BP}/{DIASTOLIC_BP} mmHg in pregnancy (threshold 140/90). Refer to the PHC for review within 48 hours.'),

       ('MAT_DANGER_SIGNS', 'Pregnancy: reported danger sign', 'MATERNAL',
        '{"match":"ANY","clauses":[{"field":"SYMPTOMS","op":"CONTAINS","value":"SEVERE_HEADACHE"},{"field":"SYMPTOMS","op":"CONTAINS","value":"BLURRED_VISION"},{"field":"SYMPTOMS","op":"CONTAINS","value":"BLEEDING"},{"field":"SYMPTOMS","op":"CONTAINS","value":"REDUCED_FETAL_MOVEMENT"}]}',
        'REFERRAL', 'CRITICAL', 12,
        'A pregnancy danger sign was reported ({SYMPTOMS}). Refer to the PHC within 12 hours.'),

       ('MAT_THIRD_TRIMESTER', 'Pregnancy: third-trimester home visit', 'MATERNAL',
        '{"match":"ALL","clauses":[{"field":"PREGNANCY_WEEKS","op":"GTE","value":28}]}',
        'FOLLOW_UP', 'MEDIUM', 336,
        'Pregnancy at {PREGNANCY_WEEKS} weeks (third trimester). Schedule the next home visit within 2 weeks.'),

       ('TB_MISSED_DOSE', 'TB: missed dose reported', 'TB',
        '{"match":"ALL","clauses":[{"field":"DOSE_TAKEN","op":"EQ","value":false}]}',
        'FOLLOW_UP', 'HIGH', 24,
        'Patient reported a missed TB dose. Visit within 24 hours to support treatment adherence.'),

       ('TB_WARNING_SYMPTOMS', 'TB: warning symptoms', 'TB',
        '{"match":"ANY","clauses":[{"field":"SYMPTOMS","op":"CONTAINS","value":"HEMOPTYSIS"},{"field":"SYMPTOMS","op":"CONTAINS","value":"BREATHLESSNESS"}]}',
        'REFERRAL', 'HIGH', 48,
        'TB patient reported a warning symptom ({SYMPTOMS}). Refer to the PHC within 48 hours.'),

       ('ANY_HIGH_FEVER', 'High fever', null,
        '{"match":"ALL","clauses":[{"field":"TEMPERATURE_C","op":"GTE","value":39.0}]}',
        'REFERRAL', 'HIGH', 24,
        'Temperature recorded as {TEMPERATURE_C} °C (threshold 39.0 °C). Refer to the PHC within 24 hours.'),

       ('CHILD_FEVER', 'Child: fever', 'CHILD_IMMUNIZATION',
        '{"match":"ALL","clauses":[{"field":"TEMPERATURE_C","op":"GTE","value":38.0}]}',
        'FOLLOW_UP', 'HIGH', 24,
        'Child temperature recorded as {TEMPERATURE_C} °C. Re-check within 24 hours.'),

       ('NCD_HIGH_BP', 'NCD: raised BP', 'NCD',
        '{"match":"ANY","clauses":[{"field":"SYSTOLIC_BP","op":"GTE","value":160},{"field":"DIASTOLIC_BP","op":"GTE","value":100}]}',
        'REFERRAL', 'MEDIUM', 72,
        'BP recorded as {SYSTOLIC_BP}/{DIASTOLIC_BP} mmHg (threshold 160/100). Refer to the PHC within 3 days.');
