# CareRoute

> Community Health Follow-Up and Referral Coordination Platform

---

## Abstract

CareRoute is a field-first digital health platform designed to support community health workers (ASHAs) and supervisors in managing patient follow-ups and referrals at the last mile. The system enables ASHAs to register patients, record visits using voice or manual input in Telugu or English, and trigger care plans based on configurable clinical rules. Supervisors can monitor referral pipelines, track escalations on a live village map, and manage the rule engine from a dedicated dashboard. CareRoute bridges the gap between frontline health workers and higher-level facilities by providing structured, role-based workflows, automated escalation with real-time push notifications and SMS alerts â€” all within a lightweight, easily deployable stack.

---

## Contributors

- **Jaswanth Kumar Reddy** - [tjaswanthkumarreddy@gmail.com](mailto:tjaswanthkumarreddy@gmail.com)
- **Childvillas** - [aichidvilas7@gmail.com](mailto:aichidvilas7@gmail.com)
- **Syed Simal** - [simal1042006@gmail.com](mailto:simal1042006@gmail.com)
- **Harsha Vardan** - [lovelyharsha8639@gmail.com](mailto:lovelyharsha8639@gmail.com)

---

## New Features

### Voice Input (Telugu / English)
ASHAs can record visit observations by speaking instead of typing. The voice button on the visit form activates the browser's Speech Recognition API in Telugu (`te-IN`). Spoken values for BP, temperature, weight, pregnancy weeks, TB dose, and symptoms are parsed and auto-filled into the form fields. Both Telugu and English phrases are supported (e.g., "à°¬à°¿à°ªà°¿ 145 over 90", "temperature 38.5", "dose missed").

### Real-Time Escalation Alerts (SSE)
When a referral escalates (patient overdue at PHC), a real-time push notification is delivered to all connected clients via Server-Sent Events. A toast notification appears instantly on screen and the dashboard, due list, and referrals are automatically refreshed â€” no manual page reload needed.

### SMS Alerts on Escalation
On referral escalation the assigned ASHA's registered phone number receives an SMS alert via Twilio ("CareRoute Alert: \<patient\> referral is overdue. Please visit today."). If Twilio credentials are not configured, the message is logged to console (mock mode).

### Village Map on Supervisor Dashboard
The supervisor dashboard now includes an SVG village map. Each village is plotted using geographic coordinates with circle size proportional to open referrals and colour indicating escalation status (red = escalated). Overdue follow-up counts are shown inside each circle.

### Telugu / English Language Toggle
All text in the visit recording form supports bilingual display. A `LangContext` stores the user's preference in `localStorage`. The ASHA can switch between English and Telugu at any time.

### Toast Notification System
A global `ToastContext` provides a `push(title, body, tone)` API used throughout the app. Toasts appear bottom-right, auto-dismiss after 6 seconds, and support `info` and `critical` tones with matching colour and icon.

### Patient Photo Support
A `photo_data` column (base64 data URI) has been added to the `patient` table via the V3 migration, enabling patient photo storage without a separate object store.

---

## How to Run the Project

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Node.js 20+](https://nodejs.org/) and npm

---

### 1. Start the Database

```bash
docker compose up -d
```

This starts a PostgreSQL 16 instance at `localhost:5432` with database name, username, and password all set to `careroute`. Flyway migrations run automatically on backend startup.

---

### 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

- API runs at: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- SSE stream: `http://localhost:8080/api/events?token=<jwt>`

> On first start, demo data (synthetic patients, users, rules) is seeded automatically.

**Environment variables (optional overrides):**

| Variable                  | Default                                      | Description                        |
|---------------------------|----------------------------------------------|------------------------------------|
| `DB_URL`                  | `jdbc:postgresql://localhost:5432/careroute` | Database URL                       |
| `DB_USERNAME`             | `careroute`                                  | Database username                  |
| `DB_PASSWORD`             | `careroute`                                  | Database password                  |
| `JWT_SECRET`              | *(dev default)*                              | Base64 secret, min 32 bytes        |
| `PORT`                    | `8080`                                       | Server port                        |
| `CORS_ORIGINS`            | `http://localhost:5173`                      | Allowed frontend origin            |
| `DEMO_ENABLED`            | `true`                                       | Seed demo data on startup          |
| `TWILIO_ACCOUNT_SID`      | *(empty)*                                    | Twilio SID for SMS alerts          |
| `TWILIO_AUTH_TOKEN`       | *(empty)*                                    | Twilio auth token                  |
| `TWILIO_FROM_NUMBER`      | *(empty)*                                    | Twilio sender number               |

> If Twilio variables are not set, SMS messages are logged to console (mock mode) â€” the app works normally.

---

### 3. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

- App runs at: `http://localhost:5173`

---

## Architecture

CareRoute follows a standard **three-tier web architecture** with clear separation between the frontend, backend API, and database. Communication between tiers is via JSON REST over HTTP, secured with JWT bearer tokens. Real-time events are pushed from backend to frontend via Server-Sent Events (SSE). The schema is fully managed by Flyway migrations so the database is always in a known state on startup.

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                        Browser / Client                     â”‚
â”‚              React 19 + TypeScript + Tailwind CSS           â”‚
â”‚       (Vite Â· React Router Â· TanStack Query Â· SSE)          â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                        â”‚ HTTPS / REST (JSON)
                        â”‚ JWT Bearer Token
                        â”‚ SSE (text/event-stream)
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                      Backend API                            â”‚
â”‚            Spring Boot 3.5 Â· Java 21 Â· Maven                â”‚
â”‚                                                             â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”‚
â”‚  â”‚  Auth /  â”‚  â”‚ Patient  â”‚  â”‚ Referral â”‚  â”‚   Rules    â”‚ â”‚
â”‚  â”‚ Security â”‚  â”‚  Visit   â”‚  â”‚ Follow-upâ”‚  â”‚  Engine    â”‚ â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â”‚
â”‚                                                             â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”‚
â”‚  â”‚Dashboard â”‚  â”‚ CarePlan â”‚  â”‚   SSE /  â”‚  â”‚Notificationâ”‚ â”‚
â”‚  â”‚ Service  â”‚  â”‚ Service  â”‚  â”‚  Events  â”‚  â”‚  + SMS     â”‚ â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                        â”‚ JPA / Flyway
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                      PostgreSQL 16                          â”‚
â”‚              Schema managed by Flyway migrations            â”‚
â”‚                  (Docker Â· persistent volume)               â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

### Backend Modules

| Module         | Responsibility                                                                  |
|----------------|---------------------------------------------------------------------------------|
| `auth`         | Login, JWT issuance                                                             |
| `security`     | JWT filter, SSE token filter (query-param JWT for EventSource), RBAC            |
| `patient`      | Patient registration, search, profile management, photo storage (base64)        |
| `visit`        | Visit recording, symptom capture, voice source tracking (`MANUAL` / `VOICE`)    |
| `referral`     | Referral lifecycle, status transitions, auto-escalation job                     |
| `followup`     | Follow-up scheduling and status tracking                                        |
| `careplan`     | Rule-driven care plan generation from visit inputs                              |
| `rules`        | Configurable rule engine (conditions, operators, actions)                       |
| `dashboard`    | Aggregated supervisor metrics, KPIs, village map coordinates                    |
| `audit`        | Immutable audit log for key actions                                             |
| `demo`         | Demo data seeding and reset endpoints                                           |
| `location`     | Village and facility reference data with lat/lng coordinates                    |
| `user`         | App user management and roles (ASHA, PHC_STAFF, SUPERVISOR)                    |
| `sse`          | Server-Sent Events â€” subscribe endpoint + broadcast to all connected clients    |
| `notification` | Orchestrates SSE push + Twilio SMS on referral escalation events                |

### Frontend Structure

| Layer        | Files / Folders                                 | Purpose                                          |
|--------------|-------------------------------------------------|--------------------------------------------------|
| Pages        | `src/pages/`                                    | Route-level components per role                  |
| Components   | `src/components/`                               | Shared UI (AppShell, VoiceButton, StatusBadgesâ€¦) |
| Contexts     | `src/contexts/LangContext.tsx`                  | Bilingual (English / Telugu) toggle              |
|              | `src/contexts/ToastContext.tsx`                 | Global toast notifications                       |
| Hooks        | `src/hooks/useVoiceInput.ts`                    | Speech Recognition + Telugu/English NLP parser   |
|              | `src/hooks/useSSE.ts`                           | EventSource subscription + cache invalidation    |
| API          | `src/api/queries.ts`                            | TanStack Query wrappers for all endpoints        |
| Auth         | `src/auth/`                                     | JWT storage, AuthContext, RequireAuth guard       |
| Lib          | `src/lib/`                                      | HTTP client, formatters, label maps, rules UI    |

### Frontend Pages

| Role       | Pages                                                         |
|------------|---------------------------------------------------------------|
| ASHA       | Due List, Register Patient, Record Visit (with voice input)   |
| PHC Staff  | Referrals, Patient list                                       |
| Supervisor | Dashboard (with village map), Patients, Referrals, Rules      |
| Shared     | Login, Patient Detail, Referral Detail, Not Found             |

### Tech Stack Summary

| Layer      | Technology                                                            |
|------------|-----------------------------------------------------------------------|
| Frontend   | React 19, TypeScript, Vite 7, Tailwind CSS 4, TanStack Query, Axios  |
| Backend    | Spring Boot 3.5, Java 21, Spring Security, Spring Data JPA            |
| Database   | PostgreSQL 16, Flyway (schema migrations)                             |
| Auth       | JWT (JJWT 0.12), role-based access control                            |
| Real-time  | Server-Sent Events (SSE) via Spring's `SseEmitter`                    |
| SMS        | Twilio REST API (mock/log mode when credentials not set)              |
| Voice      | Web Speech API (`SpeechRecognition`, `te-IN` language)                |
| API Docs   | SpringDoc OpenAPI / Swagger UI                                        |
| Dev Infra  | Docker Compose                                                        |

---

## Roles & Permissions

CareRoute uses three roles enforced at the API level via Spring Security.

| Role         | Access                                                                                       |
|--------------|----------------------------------------------------------------------------------------------|
| `ASHA`       | Register patients, record visits (manual + voice), view own due list, track referrals        |
| `PHC_STAFF`  | Manage incoming referrals (acknowledge, arrived, close), view patients in facility catchment |
| `SUPERVISOR` | All access + manage rules, view all patients/referrals, dashboard, village map, reset demo   |

All endpoints except `/api/auth/login` and `/actuator/health` require a valid JWT bearer token. Role checks are applied per-endpoint using `@PreAuthorize` annotations and a centralised `AccessPolicy` component.

---

## API Endpoints

A full interactive reference is available at `http://localhost:8080/swagger-ui.html` once the backend is running.

| Group        | Base Path           | Description                                        |
|--------------|---------------------|----------------------------------------------------|
| Auth         | `/api/auth`         | Login, current user                                |
| Patients     | `/api/patients`     | Register, search, get by ID, access log            |
| Visits       | `/api/patients/:id/visits` | Record a visit (manual or voice source)     |
| Referrals    | `/api/referrals`    | List, get, acknowledge, arrive, close              |
| Follow-ups   | `/api/follow-ups`   | Due list, complete                                 |
| Rules        | `/api/rules`        | CRUD, toggle active, list                          |
| Dashboard    | `/api/dashboard`    | Supervisor KPIs, funnel, village data with coords  |
| Meta         | `/api/meta`         | Reference data (symptoms, condition fields, enums) |
| Events       | `/api/events`       | SSE stream (`?token=<jwt>` for EventSource)        |
| Demo         | `/api/demo`         | Fast-forward time, reset data (SUPERVISOR only)    |
| Health       | `/actuator/health`  | Service health check (public)                      |

---

## Database Schema

The schema is owned and versioned by **Flyway** migrations in `backend/src/main/resources/db/migration/`. Hibernate runs in `validate` mode â€” it checks entities match the schema but never modifies tables.

| Migration File            | Description                                                          |
|---------------------------|----------------------------------------------------------------------|
| `V1__init_schema.sql`     | Core tables: users, patients, visits, referrals, follow-ups, rules   |
| `V2__reference_data.sql`  | Seed data: villages, facilities, demo users, default clinical rules  |
| `V3__add_features.sql`    | `lat`/`lng` on village (village map), `photo_data` on patient        |

Key tables:

| Table         | Description                                                    |
|---------------|----------------------------------------------------------------|
| `app_user`    | Authenticated users with roles and phone numbers               |
| `patient`     | Demographics, program enrollment, photo (base64), consent      |
| `visit`       | Vitals, symptoms, notes, source (`MANUAL` or `VOICE`)          |
| `referral`    | Referral requests, status transitions, escalation timestamp    |
| `follow_up`   | Scheduled ASHA tasks, linked to rules and referrals            |
| `care_rule`   | Configurable clinical rules (conditions, actions, priorities)  |
| `audit_log`   | Immutable record of key system actions                         |
| `village`     | Reference data with lat/lng for the dashboard village map      |
| `facility`    | Health facilities for referral routing                         |

---

## Security

- **Authentication** â€” Stateless JWT-based auth. Tokens expire after 12 hours (configurable via `app.jwt.expiration`).
- **SSE Auth** â€” `EventSource` cannot send headers, so the JWT is accepted as a `?token=` query parameter on `/api/events` via `SseTokenFilter`, which rewrites it into an `Authorization: Bearer` header before the JWT filter runs.
- **JWT Secret** â€” A dev-only default is bundled for local use. Set `JWT_SECRET` (base64, â‰¥ 32 bytes) for any shared deployment.
- **Authorization** â€” Role-based access control via Spring Security `@PreAuthorize` and a centralised `AccessPolicy`. ASHAs can only access their own village's patients.
- **CORS** â€” Configured via `CORS_ORIGINS` (default: `http://localhost:5173`).
- **Password Storage** â€” BCrypt hashed.
- **Error Responses** â€” Stack traces never included (`include-stacktrace: never`).

---

## Demo Data

When `DEMO_ENABLED=true` (default), the backend seeds synthetic data on first startup:

- Users for each role with known credentials
- ~16 patients per village across 4 health programs (Maternal, TB, Child, NCD)
- Visit history with realistic observations and rule-generated outcomes
- Referrals in various lifecycle states (open, acknowledged, arrived, closed, escalated)
- Pending and overdue follow-ups
- Default clinical rules for the rule engine
- Village coordinates for the dashboard map (Ramapuram, Kondapalli, Gollapalli â€” Andhra Pradesh)

**Demo accounts (password for all: `demo@123`):**

| Username           | Name       | Role        | Scope              |
|--------------------|------------|-------------|--------------------|
| `asha.sunitha`     | Sunitha K  | ASHA        | Ramapuram village  |
| `asha.padma`       | Padma R    | ASHA        | Kondapalli village |
| `asha.rani`        | Rani M     | ASHA        | Gollapalli village |
| `phc.kiran`        | Dr. Kiran V| PHC staff   | Ramapuram PHC      |
| `supervisor.anil`  | Anil P     | Supervisor  | Ramapuram Block    |

To reset demo data: `POST /api/demo/reset` (requires SUPERVISOR role).

> Demo mode is for development and evaluation only. Set `DEMO_ENABLED=false` for real deployments.

---

## License

This project is developed by **Innovation Matrix** as a community health initiative. All rights reserved.

For questions, contributions, or collaboration inquiries, reach out to any of the contributors listed above.

---

*Built with care for frontline health workers.*
