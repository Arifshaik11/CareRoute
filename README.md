# CareRoute

> Community Health Follow-Up and Referral Coordination Platform

---

## Abstract

CareRoute is a field-first digital health platform designed to support community health workers (ASHAs) and supervisors in managing patient follow-ups and referrals at the last mile. The system enables ASHAs to register patients, record visits, and trigger care plans based on configurable clinical rules. Supervisors can monitor referral pipelines, track escalations, and manage the rule engine from a dedicated dashboard. CareRoute bridges the gap between frontline health workers and higher-level facilities by providing structured, role-based workflows, automated escalation, and real-time coordination — all within a lightweight, easily deployable stack.

---

## Contributors

- **Jaswanth Kumar Reddy** - [tjaswanthkumarreddy@gmail.com](mailto:tjaswanthkumarreddy@gmail.com)
- **Childvillas** - [aichidvilas7@gmail.com](mailto:aichidvilas7@gmail.com)
- **Syed Simal** - [simal1042006@gmail.com](mailto:simal1042006@gmail.com)
- **harsha vardan** - [lovelyharsha8639@gmail.com](mailto:lovelyharsha8639@gmail.com)

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

> On first start, demo data (synthetic patients, users, rules) is seeded automatically.

**Environment variables (optional overrides):**

| Variable        | Default                                      | Description                 |
|-----------------|----------------------------------------------|-----------------------------|
| `DB_URL`        | `jdbc:postgresql://localhost:5432/careroute` | Database URL                |
| `DB_USERNAME`   | `careroute`                                  | Database username           |
| `DB_PASSWORD`   | `careroute`                                  | Database password           |
| `JWT_SECRET`    | *(dev default)*                              | Base64 secret, min 32 bytes |
| `PORT`          | `8080`                                       | Server port                 |
| `CORS_ORIGINS`  | `http://localhost:5173`                      | Allowed frontend origin     |
| `DEMO_ENABLED`  | `true`                                       | Seed demo data on startup   |

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

CareRoute follows a standard **three-tier web architecture** with clear separation between the frontend, backend API, and database. Communication between tiers is via JSON REST over HTTP, secured with JWT bearer tokens. The schema is fully managed by Flyway migrations so the database is always in a known state on startup.

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser / Client                     │
│              React 19 + TypeScript + Tailwind CSS           │
│           (Vite · React Router · TanStack Query)            │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / REST (JSON)
                            │ JWT Bearer Token
┌───────────────────────────▼─────────────────────────────────┐
│                      Backend API                            │
│            Spring Boot 3.5 · Java 21 · Maven                │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐ │
│  │  Auth /  │  │ Patient  │  │ Referral │  │   Rules    │ │
│  │ Security │  │  Visit   │  │ Follow-up│  │  Engine    │ │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘ │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐ │
│  │Dashboard │  │ CarePlan │  │  Audit   │  │  OpenAPI   │ │
│  │ Service  │  │ Service  │  │   Log    │  │ /Swagger   │ │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘ │
└───────────────────────────┬─────────────────────────────────┘
                            │ JPA / Flyway
┌───────────────────────────▼─────────────────────────────────┐
│                      PostgreSQL 16                          │
│              Schema managed by Flyway migrations            │
│                  (Docker · persistent volume)               │
└─────────────────────────────────────────────────────────────┘
```

### Backend Modules

| Module      | Responsibility                                                      |
|-------------|---------------------------------------------------------------------|
| `auth`      | Login, JWT issuance                                                 |
| `security`  | JWT filter, role-based access control, user details                 |
| `patient`   | Patient registration, search, profile management                    |
| `visit`     | Visit recording, symptom capture                                    |
| `referral`  | Referral lifecycle, status transitions, auto-escalation job         |
| `followup`  | Follow-up scheduling and status tracking                            |
| `careplan`  | Rule-driven care plan generation from visit inputs                  |
| `rules`     | Configurable rule engine (conditions, operators, actions)           |
| `dashboard` | Aggregated supervisor metrics and KPIs                              |
| `audit`     | Immutable audit log for key actions                                 |
| `demo`      | Demo data seeding and reset endpoints                               |
| `location`  | Village and facility reference data                                 |
| `user`      | App user management and roles (ASHA, SUPERVISOR, ADMIN)             |

### Frontend Pages

| Role       | Pages                                              |
|------------|----------------------------------------------------|
| ASHA       | Due List, Register Patient, Record Visit           |
| Supervisor | Dashboard, Patients, Referrals, Rules, Rule Editor |
| Shared     | Login, Patient Detail, Referral Detail             |

### Tech Stack Summary

| Layer      | Technology                                                      |
|------------|-----------------------------------------------------------------|
| Frontend   | React 19, TypeScript, Vite, Tailwind CSS, TanStack Query, Axios |
| Backend    | Spring Boot 3.5, Java 21, Spring Security, Spring Data JPA      |
| Database   | PostgreSQL 16, Flyway (schema migrations)                       |
| Auth       | JWT (JJWT 0.12), role-based access control                      |
| API Docs   | SpringDoc OpenAPI / Swagger UI                                  |
| Dev Infra  | Docker Compose                                                  |

---

## Roles & Permissions

CareRoute uses three roles enforced at the API level via Spring Security.

| Role         | Access                                                                                       |
|--------------|----------------------------------------------------------------------------------------------|
| `ASHA`       | Register patients, record visits, view own due list, create referrals                        |
| `SUPERVISOR` | All ASHA access + manage rules, view all patients/referrals, access dashboard, reset demo    |
| `ADMIN`      | Full access including user management and audit log                                          |

All endpoints except `/api/auth/login` and `/actuator/health` require a valid JWT bearer token. Role checks are applied per-endpoint using `@PreAuthorize` annotations and a centralised `AccessPolicy` component.

---

## API Endpoints

A full interactive reference is available at `http://localhost:8080/swagger-ui.html` once the backend is running. Key endpoint groups are listed below.

| Group        | Base Path           | Description                                      |
|--------------|---------------------|--------------------------------------------------|
| Auth         | `/api/auth`         | Login, token refresh                             |
| Patients     | `/api/patients`     | Register, search, get by ID                      |
| Visits       | `/api/visits`       | Record a visit, list visits for a patient        |
| Referrals    | `/api/referrals`    | Create, update status, list, escalate            |
| Follow-ups   | `/api/followups`    | Schedule, complete, list due follow-ups          |
| Rules        | `/api/rules`        | CRUD for clinical rules, evaluate rule set       |
| Dashboard    | `/api/dashboard`    | Supervisor KPIs and summary metrics              |
| Meta         | `/api/meta`         | Reference data (villages, facilities, enums)     |
| Demo         | `/api/demo`         | Seed or reset demo data (supervisor only)        |
| Health       | `/actuator/health`  | Service health check (public)                    |

---

## Database Schema

The schema is owned and versioned by **Flyway** migrations located in `backend/src/main/resources/db/migration/`. Hibernate runs in `validate` mode — it only checks that the schema matches the entities, it never modifies tables.

| Migration File         | Description                                                         |
|------------------------|---------------------------------------------------------------------|
| `V1__init_schema.sql`  | Core tables: users, patients, visits, referrals, follow-ups, rules  |
| `V2__reference_data.sql` | Seed data: villages, facilities, default clinical rules            |

Key tables:

| Table         | Description                                         |
|---------------|-----------------------------------------------------|
| `app_user`    | Authenticated users with roles                      |
| `patient`     | Patient demographics and program enrollment         |
| `visit`       | Visit records with symptoms and vitals              |
| `referral`    | Referral requests and status transitions            |
| `follow_up`   | Scheduled follow-up tasks linked to patients        |
| `care_rule`   | Configurable clinical rules for care plan triggers  |
| `audit_log`   | Immutable record of key system actions              |
| `village`     | Reference data for patient locations                |
| `facility`    | Health facilities for referral routing              |

---

## Security

- **Authentication** — Stateless JWT-based auth. Tokens are issued on login (`/api/auth/login`) and expire after 12 hours by default (configurable via `app.jwt.expiration`).
- **JWT Secret** — A dev-only default secret is bundled for local use. For any shared or production deployment, set the `JWT_SECRET` environment variable to a base64-encoded secret of at least 32 bytes.
- **Authorization** — Role-based access control is enforced at the controller layer using Spring Security's `@PreAuthorize` and a centralised `AccessPolicy` component. ASHAs can only access their own patients and visits.
- **CORS** — Allowed origins are configured via `CORS_ORIGINS` (default: `http://localhost:5173`). Restrict this to your production frontend URL before deploying.
- **Password Storage** — Passwords are hashed with BCrypt before being stored in the database.
- **Error Responses** — Stack traces are never included in API error responses (`include-stacktrace: never`).

---

## Demo Data

When `DEMO_ENABLED=true` (the default), the backend seeds a realistic set of synthetic data on first startup via `DemoDataSeeder`. This includes:

- Pre-created users for each role with known credentials
- A set of patients across different villages and health programs
- Sample visits with symptoms and vitals
- Active referrals in various lifecycle states
- Scheduled follow-ups with pending and overdue entries
- Default clinical rules for the rule engine

**Demo users (default credentials):**

| Username     | Password   | Role         |
|--------------|------------|--------------|
| `asha1`      | `password` | `ASHA`       |
| `supervisor` | `password` | `SUPERVISOR` |
| `admin`      | `password` | `ADMIN`      |

To reset demo data at any time, call `POST /api/demo/reset` (requires `SUPERVISOR` role or above).

> Demo mode is intended for development and evaluation only. Disable it (`DEMO_ENABLED=false`) for any real deployment.

---

## License

This project is developed by **Innovation Matrix** as a community health initiative. All rights reserved.

For questions, contributions, or collaboration inquiries, reach out to any of the contributors listed above.

---

*Built with care for frontline health workers.*
