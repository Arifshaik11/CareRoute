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
