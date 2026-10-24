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
