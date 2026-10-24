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

