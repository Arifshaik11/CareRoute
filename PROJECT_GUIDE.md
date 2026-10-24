# CareRoute - Complete Project Guide

CareRoute is a community health follow-up system that connects village health workers (ASHAs) with primary health centers (PHCs). The core idea: no patient should fall through the cracks between the village and the clinic.

The app runs at **http://localhost:5173** (frontend) and talks to the API at **http://localhost:8080** (backend).

---

## Quick Start

1. Open **http://localhost:5173** in your browser
2. You'll see the login page with three demo account buttons
3. Click any demo account button to sign in (password is pre-filled: `demo@123`)
4. Each role lands on a different home page and sees a different sidebar

---

## Demo Accounts

All accounts share the same password: **`demo@123`**

| Username | Name | Role | Scope |
|---|---|---|---|
| `asha.sunitha` | Sunitha K | ASHA worker | Ramapuram village |
| `asha.padma` | Padma R | ASHA worker | Kondapalli village |
| `asha.rani` | Rani M | ASHA worker | Gollapalli village |
| `phc.kiran` | Dr. Kiran V | PHC staff | Ramapuram PHC |
| `supervisor.anil` | Anil P | Block supervisor | Ramapuram Block |

The login page shows three quick-login buttons for Sunitha (ASHA), Dr. Kiran (PHC), and Anil (Supervisor). You can also type any username manually, including `asha.padma` or `asha.rani`.

---

## The Three Roles

### 1. ASHA Worker (asha.sunitha)

**Who they are:** A community health worker assigned to one village. They visit patients at home, record observations, and follow up on tasks the system assigns them.

**Home page:** `/due` (Due List) - a single prioritised list of visits they need to make, most urgent first.

**Sidebar / navigation:**
- **Due list** - prioritised tasks (overdue, due today, upcoming)
- **Patients** - patients assigned to them in their village
- **Referrals** - referrals for their patients (read-only, to track status)

**What they can do:**
- See a prioritised due list with three sections: Overdue, Due today, Upcoming
- Each task card shows the patient name, program, priority, why the task exists (which rule fired), and when it's due
- Click "Record visit" on any task to go to the visit recording form
- Click "Mark done" to complete a follow-up without recording a visit
- Register a new patient (name, age, sex, program, phone, consent required)
- Record a home visit with observations: blood pressure, temperature, weight, pregnancy weeks, TB dose, symptoms, notes
- After saving a visit, see what the rules engine decided (follow-up created? referral sent?)
- View patient detail pages with visit history, open tasks, and referral timelines
- View referrals for their patients (read-only, to see if the PHC acknowledged/received the patient)

**What they cannot do:**
- See patients from other villages
- See the block dashboard
- Create or edit rules
- Update referral status (that's PHC staff's job)

---

### 2. PHC Staff (phc.kiran)

**Who they are:** Staff at the primary health center. They receive referrals from ASHAs and track patients through the facility.

**Home page:** `/referrals` - incoming referrals to their PHC, with tabs for different statuses.

**Sidebar / navigation:**
- **Referrals** - incoming referrals with tabs: Awaiting arrival, Escalated, Arrived, Closed, All
- **Patients** - patients from villages their facility serves (read-only, no "record visit" button)

**What they can do:**
- View all referrals to their facility, filtered by status using the tab bar
- Open a referral to see the patient info, reason, timeline, and priority
- **Acknowledge** a referral (status: REFERRED -> ACKNOWLEDGED) - signals "we're expecting this patient"
- **Mark patient arrived** (status: ACKNOWLEDGED -> ARRIVED) - confirms the patient reached the facility
- **Close with outcome** (status: ARRIVED -> CLOSED) - requires an outcome note, completes the referral loop
- See the referral timeline showing every event (referred, acknowledged, arrived, escalated, closed)
- See which referrals are overdue (patient hasn't arrived by deadline) and escalated

**The referral lifecycle they manage:**

```
REFERRED  --[Acknowledge]-->  ACKNOWLEDGED  --[Patient arrived]-->  ARRIVED  --[Close with outcome]-->  CLOSED
```

If a referral is in REFERRED status, they also see a "Patient already here" button to skip straight to arrived.

**What they cannot do:**
- Record visits (only ASHAs do that)
- Register patients
- See the dashboard
- Create or edit rules

---

### 3. Block Supervisor (supervisor.anil)

**Who they are:** A block-level medical officer who oversees the whole system, monitors performance, and configures the clinical rules.

**Home page:** `/dashboard` - a block-wide overview with KPIs, referral funnel, village breakdown, and active escalations.

**Sidebar / navigation:**
- **Dashboard** - KPIs, referral funnel, village table, active escalations, demo controls
- **Referrals** - all referrals in the block (read-only, no action buttons)
- **Patients** - all patients in the block (read-only, with access log showing who viewed each record)
- **Rules** - list of all follow-up and referral rules, with toggle and edit

**What they can do:**
- See block-level KPIs: referral completion rate, escalated referrals, overdue follow-ups, visits in last 7 days
- See the referral funnel: Referred -> Acknowledged -> Arrived -> Closed, showing where patients drop off
- See a per-village table showing patients, overdue tasks, open referrals, escalations, and completion rate per ASHA
- See active escalations (referrals that are overdue with the patient not at the facility)
- View all patients with full access logs (who viewed/changed each record and when)
- **Create new rules** (name, code, program, condition clauses, action, priority, deadline, explanation)
- **Edit existing rules** (change thresholds, priorities, deadlines, explanations)
- **Toggle rules on/off** (activate or deactivate without deleting)
- **Fast-forward 48 hours** (demo control: moves all deadlines 48 hours closer and runs escalation, simulating two days passing)
- **Reset demo data** (wipes and re-seeds all synthetic patients)

**What they cannot do:**
- Record visits
- Register patients
- Update referral status (that's PHC staff's job)

---

## The Core Workflow (How Everything Connects)

```
                         ┌─────────────────────────────────────────────────┐
                         │              RULES ENGINE                        │
                         │  (configured by Supervisor, runs on every visit) │
                         └──────────┬──────────────────────┬────────────────┘
                                    │                      │
                          FOLLOW_UP rule              REFERRAL rule
                                    │                      │
                                    ▼                      ▼
                        ┌──────────────────┐   ┌──────────────────────┐
                        │  Task added to    │   │  Referral sent to    │
                        │  ASHA's due list  │   │  the PHC             │
                        └──────────────────┘   └──────────┬───────────┘
                                                            │
                                                  PHC staff sees it
                                                            │
                                       ┌────────────────────┼────────────────────┐
                                       ▼                    ▼                    ▼
                                  Acknowledge         Patient arrived       Close with outcome
                                                            │
                                            ┌─────────────┴──────────────┐
                                            │                            │
                                     Patient arrives              Patient doesn't arrive
                                            │                            │
                                            ▼                            ▼
                                     Loop closed              ESCALATION fires
                                    (resolved)                (after deadline)
                                                                      │
                                                                      ▼
                                                        ┌──────────────────────┐
                                                        │ Critical task sent   │
                                                        │ back to ASHA's due   │
                                                        │ list: "Patient hasn't│
                                                        │ arrived, go help"    │
                                                        └──────────────────────┘
```

**Step by step:**
1. **ASHA records a visit** with observations (BP, temperature, weight, symptoms, etc.)
2. The visit **completes any pending follow-ups** for that patient (a visit fulfils outstanding tasks)
3. The **rules engine** evaluates the visit against all active rules
4. Each matching **FOLLOW_UP rule** creates a new task on the ASHA's due list with a deadline
5. The most urgent matching **REFERRAL rule** creates a referral to the PHC (only one referral at a time per patient)
6. **PHC staff** see the referral and work it through: Acknowledge -> Patient arrived -> Close with outcome
7. If the patient **doesn't arrive by the deadline**, the referral **escalates**: a critical task appears on the ASHA's due list saying "the patient hasn't reached the PHC, go help them"
8. The **supervisor's dashboard** shows the whole picture: how many referrals completed, how many escalated, village-by-village breakdown

---

## Starter Rules (Pre-configured)

These are the 9 rules seeded by default. The supervisor can edit, toggle, or add new ones.

| Code | Name | Program | Condition | Action | Priority | Deadline |
|---|---|---|---|---|---|---|
| MAT_SEVERE_BP | Pregnancy: severely raised BP | Maternal | Systolic >= 160 OR Diastolic >= 110 | Refer | Critical | 12 hours |
| MAT_HIGH_BP | Pregnancy: raised BP | Maternal | Systolic >= 140 OR Diastolic >= 90 | Refer | High | 48 hours |
| MAT_DANGER_SIGNS | Pregnancy: danger sign | Maternal | Symptoms include severe headache, blurred vision, bleeding, or reduced fetal movement | Refer | Critical | 12 hours |
| MAT_THIRD_TRIMESTER | Pregnancy: third-trimester visit | Maternal | Pregnancy weeks >= 28 | Follow up | Medium | 336 hours (2 weeks) |
| TB_MISSED_DOSE | TB: missed dose | TB | Dose taken = false | Follow up | High | 24 hours |
| TB_WARNING_SYMPTOMS | TB: warning symptoms | TB | Symptoms include hemoptysis or breathlessness | Refer | High | 48 hours |
| ANY_HIGH_FEVER | High fever | All | Temperature >= 39.0 C | Refer | High | 24 hours |
| CHILD_FEVER | Child: fever | Child health | Temperature >= 38.0 C | Follow up | High | 24 hours |
| NCD_HIGH_BP | NCD: raised BP | NCD | Systolic >= 160 OR Diastolic >= 100 | Refer | Medium | 72 hours |

---

## Test Scenarios (Step-by-Step)

### Scenario 1: ASHA records a routine maternal visit (follow-up created)

**Goal:** See how a normal visit creates a follow-up task without triggering a referral.

1. Open http://localhost:5173
2. Click the **Sunitha K** demo button (ASHA, Ramapuram village)
3. You land on the **Due List** page. You'll see a greeting "Namaskaram, Sunitha" with three counters (Overdue, Due today, Upcoming)
4. The due list is sorted: Overdue first, then Due today, then Upcoming
5. Click on any patient name in the due list (this opens their patient detail page)
6. On the patient detail page, click **"Record visit"** (top right button)
7. On the visit form, enter normal values:
   - Blood pressure: **118** / **76** (normal for pregnancy)
   - Temperature: **36.8**
   - Weight: **55**
   - Pregnancy weeks: leave as estimated (auto-filled from last visit)
   - Don't check any symptoms
8. Click **"Save visit"**
9. You'll see the **Visit saved** screen showing:
   - "Next visit scheduled" (because MAT_THIRD_TRIMESTER rule fired if weeks >= 28)
   - Under "Why": the rule name, its explanation, and the outcome "Follow-up added to due list"
   - A note about the next due date
10. Click **"Back to due list"** - the new follow-up task will appear in your list

**What happened behind the scenes:** The CarePlanService saved the visit, completed any pending follow-ups, ran all active rules, and the MAT_THIRD_TRIMESTER rule (if applicable) created a new medium-priority follow-up due in 2 weeks.

---

### Scenario 2: ASHA records a high BP visit (referral triggered)

**Goal:** See how abnormal readings trigger a referral to the PHC. This is the key scenario with the demo patient Lakshmi Devi.

1. Log in as **Sunitha K** (ASHA)
2. On the Due List, look for **Lakshmi Devi** (24 y, Maternal care, Ramapuram). She's specifically seeded with a follow-up due in about 4 hours, so she should appear in "Due today" or "Upcoming"
3. Click her name to open her patient detail page
4. Click **"Record visit"**
5. Enter raised blood pressure:
   - BP: **145** / **92** (above the 140/90 threshold for MAT_HIGH_BP rule)
   - Temperature: **36.8**
   - Weight: **55**
   - Pregnancy weeks: leave as estimated
   - Don't check any symptoms
6. Click **"Save visit"**
7. You'll see:
   - A red card: **"Referral sent to the PHC"** with a warning message about the deadline
   - Under "Why": the **MAT_HIGH_BP** rule fired, showing "BP recorded as 145/92 mmHg in pregnancy (threshold 140/90). Refer to the PHC for review within 48 hours."
   - Priority badge: **HIGH**
   - Outcome: **"Referral sent to PHC"**
   - A note saying the PHC can see this referral now, and if the patient hasn't arrived by the deadline, it will come back to your due list
8. Click **"View patient"** to see the referral appear in the patient's referral list with status "Referred"
9. Click **"Back to due list"** - note that Lakshmi's original due task is now completed (the visit fulfilled it)

**What happened:** The MAT_HIGH_BP rule matched (systolic 145 >= 140), creating a HIGH priority referral to Ramapuram PHC with a 48-hour deadline.

---

### Scenario 3: PHC staff processes the referral (full loop)

**Goal:** See how PHC staff receive and work through a referral that an ASHA just created.

1. Log in as **Dr. Kiran V** (PHC staff) - you'll land on the **Referrals** page
2. You'll see tabs: **Awaiting arrival** (count), **Escalated**, **Arrived**, **Closed**, **All**
3. The referral from Scenario 2 (Lakshmi Devi) should appear under "Awaiting arrival" with status **Referred** and priority **High**
4. Click on the referral to open its detail page
5. You'll see:
   - Patient name, age, program, village, ASHA name
   - The reason the referral was created (BP threshold exceeded)
   - Referred timestamp and expected-by deadline
   - A **timeline** on the right showing the "Referred" event
6. Under "Update referral", you'll see the action button: **"Acknowledge - expecting patient"**
7. Click it - the status changes to **Acknowledged**, and a new event appears on the timeline
8. The button now changes to: **"Patient has arrived"**
9. Click it - status changes to **Arrived**, timeline updates
10. The button now changes to: **"Close with outcome"** with a required outcome note field
11. Type an outcome note, e.g., "Reviewed by MO, BP monitoring advised, follow-up in 1 week"
12. Click the button - status changes to **Closed**, the loop is complete
13. Go back to the Referrals page and check the "Closed" tab - the referral appears there

**What happened:** The referral moved through REFERRED -> ACKNOWLEDGED -> ARRIVED -> CLOSED. Each step was audited and added to the referral's event timeline.

---

### Scenario 4: The escalation loop (patient doesn't arrive)

**Goal:** See what happens when a referred patient doesn't reach the PHC in time. This is the "closed loop" that makes CareRoute different from a simple referral system.

1. Log in as **Sunitha K** (ASHA) and record a visit with high BP for Lakshmi Devi (same as Scenario 2) to create a referral
2. Log out (click the sign-out icon, top right)
3. Log in as **supervisor.anil** (Block supervisor)
4. You'll land on the **Dashboard**. Scroll down to the **"Demo controls"** card (bottom right, dashed border)
5. Click **"Fast-forward 48 hours"** - this simulates 2 days passing:
   - All open deadlines shift 48 hours closer
   - The escalation job runs
   - You'll see a message: "Done - X referral(s) escalated"
6. Look at the dashboard:
   - The **"Escalated referrals"** KPI should now show a number (was 0 before)
   - The **referral funnel** will show the gap (patients referred but not arrived)
   - The **"Active escalations"** section (bottom left) will list the escalated referral
7. Log out and log back in as **Sunitha K** (ASHA)
8. Go to your **Due List** - you'll see a new **CRITICAL** priority task:
   - A red "Missed referral" badge
   - The reason says: "Referral to Ramapuram PHC is overdue and the patient has not arrived. Visit today and help them reach the facility."
   - This task appeared because the referral escalated back to the ASHA
9. Click on the patient to see the referral with an "Escalated" badge

**What happened:** When a referral passes its deadline without the patient arriving at the PHC, the ReferralService.escalateOverdue() method fires. It marks the referral as escalated and creates a CRITICAL priority follow-up on the ASHA's due list, closing the loop back to the village.

---

### Scenario 5: ASHA registers a new patient

**Goal:** See how a new patient enters the system.

1. Log in as **Sunitha K** (ASHA)
2. Click **"Register patient"** (button on the Due List page, top right)
3. Fill in the form:
   - Full name: e.g., "Test Patient"
   - Age: 30
   - Sex: Female
   - Programme: Maternal care (or any)
   - Mobile number: optional (must be 10 digits starting with 6-9)
   - Notes: optional
4. Check the **consent checkbox** (required - explains what data is recorded and who can see it)
5. Click **"Register & record first visit"**
6. You're automatically taken to the **Record Visit** page for the new patient
7. Record the first visit with any values
8. After saving, the visit outcome screen shows what rules fired

**What happened:** A new patient was created in the database, assigned to your village and to you as the ASHA. The first visit immediately runs through the rules engine.

---

### Scenario 6: Supervisor creates a new rule

**Goal:** See how the supervisor configures the rules that drive the system.

1. Log in as **supervisor.anil**
2. Click **"Rules"** in the sidebar
3. You'll see all 9 starter rules as cards, each showing:
   - Name and code
   - Priority badge, action (Refer/Follow up within X hours), program
   - The condition in plain English: "If systolic_bp >= 160 OR diastolic_bp >= 110"
   - The explanation shown to staff
   - A toggle switch (Active/Off)
   - A pencil icon to edit
4. Click **"New rule"** (top right)
5. Fill in:
   - Name: e.g., "Maternal: low weight"
   - Code: MAT_LOW_WEIGHT (auto-uppercased)
   - Applies to: Maternal care
   - Priority: Medium
6. Under **Condition**:
   - Match mode: "Any clause" (OR) or "All clauses" (AND)
   - First clause: Field = Weight (kg), Operator = <=, Value = 45
   - Click "Add clause" to add more if needed
7. Under the next card:
   - Then: "Add a follow-up visit for the ASHA" (FOLLOW_UP)
   - Within (hours): 72
   - Explanation: "Weight recorded as {WEIGHT_KG} kg, below 45 kg. Schedule nutrition counselling visit within 3 days."
   - Check "Active"
8. Click **"Save rule"**
9. You're returned to the Rules list - the new rule appears at the bottom
10. Now log in as an ASHA and record a visit with low weight for a maternal patient - the new rule should fire

**What happened:** The rule was saved to the database. The next time any ASHA records a visit, the rules engine will evaluate this new rule alongside all active ones. The `{WEIGHT_KG}` placeholder gets replaced with the actual measured value in the explanation shown to staff.

---

### Scenario 7: Supervisor toggles a rule off and on

**Goal:** See how rules can be deactivated without deleting them.

1. Log in as **supervisor.anil**
2. Go to **Rules**
3. Find any rule and click the **toggle switch** (right side of the card)
4. The card fades to 60% opacity and shows "Off"
5. Now log in as an ASHA and record a visit that would have triggered that rule - nothing happens
6. Go back and toggle it on - the rule is active again

---

### Scenario 8: Supervisor resets demo data

**Goal:** Start fresh with clean synthetic data after testing.

1. Log in as **supervisor.anil**
2. Go to the **Dashboard**
3. Scroll to **"Demo controls"** (bottom right, dashed border)
4. Click **"Reset synthetic data"**
5. A confirmation appears: "Yes, reset all patient data" / "Cancel"
6. Click **"Yes, reset all patient data"**
7. All patients, visits, follow-ups, and referrals are wiped and re-seeded from scratch
8. Rules and users are preserved (only patient data is reset)

---

## Navigation Map

### ASHA Worker
```
/due                    Due list (home) - prioritised tasks
  └─ click patient ──> /patients/:id          Patient detail
  └─ Record visit ────> /patients/:id/visit    Visit form
  └─ Mark done ───────> (stays on /due, task completes)
/patients               All patients in your village
  └─ click patient ──> /patients/:id          Patient detail
  └─ Register ────────> /patients/new         New patient form
                        └─ on submit ────────> /patients/:id/visit (record first visit)
/patients/:id           Patient detail (visits, tasks, referrals, consent)
  └─ Record visit ────> /patients/:id/visit
/referrals              Referrals for your patients (read-only)
  └─ click ──────────> /referrals/:id          Referral detail (read-only for ASHA)
```

### PHC Staff
```
/referrals              Referrals (home) - incoming to your PHC
  └─ tabs: Awaiting arrival | Escalated | Arrived | Closed | All
  └─ click ──────────> /referrals/:id          Referral detail
                        └─ Acknowledge ──> status becomes Acknowledged
                        └─ Patient arrived ──> status becomes Arrived
                        └─ Close with outcome ──> status becomes Closed (requires note)
/patients               Patients in villages your PHC serves (read-only)
  └─ click ──────────> /patients/:id          Patient detail (read-only, no visit button)
```

### Block Supervisor
```
/dashboard              Dashboard (home) - KPIs, funnel, villages, escalations, demo controls
/referrals              All referrals in the block (read-only)
  └─ click ──────────> /referrals/:id          Referral detail (read-only, no action buttons)
/patients               All patients in the block (read-only)
  └─ click ──────────> /patients/:id          Patient detail with access log (who viewed the record)
/rules                  All rules (list with toggle and edit)
  └─ click edit ─────> /rules/:id              Rule editor
  └─ New rule ───────> /rules/new              Rule editor (blank)
```

---

## The Demo Patient: Lakshmi Devi

Lakshmi Devi is a specifically seeded patient designed for live demos:

- **Age:** 24, Female, Maternal care program
- **Village:** Ramapuram (assigned to ASHA Sunitha)
- **Pregnancy:** Third trimester (~28 weeks)
- **History:** Two prior visits, both with normal readings (last BP was 118/76)
- **Status:** A routine follow-up is due in about 4 hours from app startup

**The demo story:** Log in as Sunitha (ASHA), find Lakshmi in the due list, record a visit with raised BP (e.g., 145/92). This triggers the MAT_HIGH_BP referral rule. Then switch to Dr. Kiran (PHC) to see and process the referral. Then switch to Anil (Supervisor) and fast-forward 48 hours to see the escalation if the patient hasn't arrived. Switch back to Sunitha to see the critical escalation task on the due list.

---

## Key Concepts to Understand

**Every task explains itself.** Each follow-up on the ASHA's due list shows which rule created it and why, in plain language. The same explanation appears on the visit outcome screen. No black boxes.

**One referral at a time.** If a patient already has an open referral, a new matching referral rule shows "Covered by referral" instead of creating a duplicate. The most urgent referral rule wins.

**The loop is closed.** A referral isn't just sent and forgotten. If the patient doesn't arrive at the PHC by the deadline, the system escalates: a CRITICAL task appears on the ASHA's due list telling them to go help the patient reach the facility. When the patient does arrive (at any point), the escalation task is automatically resolved.

**Rules are transparent and configurable.** Every flag in the system traces back to a specific rule the supervisor can see, edit, or turn off. The supervisor controls what triggers follow-ups and referrals, with plain-language explanations shown to staff.

**Scope-based access.** ASHAs only see their own village's patients. PHC staff see patients from villages their facility serves and referrals to their facility. The supervisor sees everything, including access logs showing who viewed each patient record.

**Screening, not diagnosis.** The app includes a screening notice on visit forms and patient pages: this is screening information, not a clinical diagnosis. The rules are illustrative screening thresholds for a prototype.

---

## API Reference (for testing with curl or Postman)

Base URL: `http://localhost:8080/api`

| Endpoint | Method | Auth | Purpose |
|---|---|---|---|
| `/auth/login` | POST | None | Login (returns JWT) |
| `/auth/me` | GET | Any | Get current user |
| `/meta` | GET | Any | Reference data (programs, symptoms, condition fields) |
