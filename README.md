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
