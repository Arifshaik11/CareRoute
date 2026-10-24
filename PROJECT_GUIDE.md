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
