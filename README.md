# HRMS

A small HR management system: employees, attendance, leave approval and monthly payroll, with JWT login and role-based access.

![CI](https://github.com/snehanjali01/hrms-project/actions/workflows/ci.yml/badge.svg)

**Live demo:** https://hrms-web-u0fv.onrender.com

| Username | Role |
|---|---|
| `admin` | ADMIN |
| `hr` | HR |
| `manager` | MANAGER |
| `employee` | EMPLOYEE |

Password for all demo users: `Demo@12345`

> The backend runs on a free tier and sleeps when idle, so the first login can take a minute or more.

**Stack:** Java 24, Spring Boot 4, Spring Security, PostgreSQL, Liquibase · React 19, Vite · Docker, GitHub Actions, Render, Neon

## Features
- **Auth:** stateless JWT login, BCrypt-hashed passwords, four roles
- **Employees:** CRUD with search, sorting and pagination; salary visible only to ADMIN, HR and the employee themself
- **Attendance:** check-in and check-out for the logged-in user
- **Leave:** employees submit requests; ADMIN, HR and managers approve or reject
- **Payroll:** monthly generation per employee or for everyone

## Security
- The caller's identity comes from the token, never from the request body, so one user cannot act as another
- Endpoint rules live in `SecurityConfig`: payroll is ADMIN/HR only, leave decisions need ADMIN/HR/MANAGER
- Unauthenticated requests get `401`, a wrong role gets `403`
- Secrets come from environment variables
- Known trade-off: the frontend keeps the JWT in `localStorage`, so an XSS bug would expose it. An httpOnly cookie is the next step

## Payroll rules
- Working days are Monday to Friday; day rate = salary / working days in the month
- Present is paid, a half day deducts 0.5, approved leave is paid (including leave that started in the previous month), any other working day is deducted in full
- Payroll can only be generated for completed months

## Run locally
Requirements: Java 24, PostgreSQL, Node 22. The commands below use bash syntax.

```bash
# backend (http://localhost:8080)
createdb hrms_db
export DB_PASSWORD=your-postgres-password
export JWT_SECRET=any-random-string-of-32-or-more-characters
export APP_SEED_DEMO_DATA=true   # optional: demo data on an empty database
cd hrms && ./mvnw spring-boot:run

# frontend (http://localhost:5174), in a second terminal from the repo root
cd hrms-frontend && npm ci && npm run dev
```

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/hrms_db` |
| `DB_USERNAME` | database user | `postgres` |
| `DB_PASSWORD` | database password | required |
| `JWT_SECRET` | signing key, 32+ characters | required |
| `APP_SEED_DEMO_DATA` | create demo users and attendance on an empty database | `false` |
| `CORS_ALLOWED_ORIGINS` | allowed browser origins | `http://localhost:5174` |
| `VITE_API_URL` | backend API URL used by the frontend | `http://localhost:8080/api` |

## Tests
With the database running and `DB_PASSWORD` and `JWT_SECRET` set:

```bash
cd hrms && ./mvnw verify
```

43 tests cover JWT handling, security rules, ownership checks, payroll, services and demo data. GitHub Actions runs them, plus the frontend build, on every pull request and on every push to `main`.

## Known gaps
- Public holidays are treated as working days in payroll
- No refresh tokens; the session ends when the token expires (1 hour)
- Some frontend screens still have lint warnings
