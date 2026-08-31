# Resolvyx

**A multi-tenant issue/complaint tracking platform** — built for organizations (colleges, apartments, offices, hospitals) to let their members raise issues, get them automatically routed to staff, tracked through resolution, and rated afterward.

Think of it as infrastructure any organization can plug into to manage internal complaints — one deployment serves unlimited organizations, with each organization's data kept completely isolated from every other's.

## Live Demo

- Frontend: _(add link once deployed)_
- Backend API: _(add link once deployed)_

## Features

- **Multi-tenant architecture** — any organization can sign up and get a unique join code; all data is scoped per-organization at the service layer
- **JWT authentication** with role-based access control (`ORG_ADMIN`, `STAFF`, `MEMBER`)
- **Category management** — admins define org-specific issue types
- **Ticket lifecycle** — `OPEN → ASSIGNED → IN_PROGRESS → RESOLVED`, with permission checks at every step
- **Priority scoring** — urgency automatically maps to a numeric priority
- **Staff auto-assignment** — a greedy load-balancing algorithm assigns each new ticket to whichever staff member currently has the fewest active tickets
- **SLA auto-escalation** — a scheduled background job detects tickets that missed their resolution deadline and automatically boosts their priority
- **Feedback & ratings** — ticket raisers rate the resolution (1–5 stars + comment) once resolved
- **Analytics dashboard** — resolution time, category breakdown, staff workload, and average ratings, computed via aggregation queries

## Tech Stack

**Backend:** Java 17 · Spring Boot 4 · Spring Security · Spring Data JPA · MySQL · JWT (jjwt) · BCrypt

**Frontend:** React (Vite) · Tailwind CSS · Framer Motion · Axios · React Router

## Architecture
Organization (tenant)
├── Users (ORG_ADMIN / STAFF / MEMBER)
├── Categories
└── Tickets
├── raised by a User
├── assigned to a User (optional)
└── has Feedback (optional, post-resolution)


Every Ticket and Category carries an `organization_id`, and every service method explicitly verifies that the resource being accessed belongs to the caller's own organization — this is what actually enforces tenant isolation, not just the foreign key.

## API Overview

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/signup` | Public | Creates a new org + its first ORG_ADMIN |
| POST | `/api/auth/login` | Public | Authenticates, returns a JWT |
| POST | `/api/auth/join` | Public | Joins an existing org via its code |
| POST/GET | `/api/categories` | Admin / Any | Manage categories |
| POST/GET | `/api/tickets` | Any / Any | Raise and list tickets |
| PUT | `/api/tickets/{id}/assign` | Admin | Auto-assigns to least-busy staff |
| PUT | `/api/tickets/{id}/status` | Assigned staff / Admin | Updates ticket status |
| POST | `/api/tickets/{id}/feedback` | Ticket raiser | Submits a rating |
| GET | `/api/tickets/analytics` | Admin | Org-wide performance stats |

## Running Locally

**Backend**
```bash
# requires a running MySQL instance
cd resolvyx
# update src/main/resources/application.properties with your MySQL credentials
./mvnw spring-boot:run
```
Runs on `http://localhost:8081`

**Frontend**
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`

## Author

Pratiksha Madar
