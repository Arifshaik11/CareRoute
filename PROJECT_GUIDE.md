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

