# SkillSwap — Campus Skill Exchange Platform

> A peer-to-peer skill exchange platform that lets university students teach what they know and learn what they don't — all within the same campus community.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Database Setup](#1-database-setup)
  - [2. Backend Configuration](#2-backend-configuration)
  - [3. Running the Backend](#3-running-the-backend)
  - [4. Running the Frontend](#4-running-the-frontend)
- [Default Accounts](#default-accounts)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [How It Works](#how-it-works)
- [Security](#security)
- [Troubleshooting](#troubleshooting)

---

## Overview

SkillSwap is a full-stack web application built for Nairobi University students to exchange skills with each other. A student who knows Python can teach it in exchange for learning French from another student. Sessions are requested, confirmed, completed, and reviewed — all through a clean web interface backed by a secure REST API.

The platform has two separate portals:

| Portal | Entry Point | Purpose |
|--------|-------------|---------|
| Student Portal | `dashboard.html` | Browse skills, manage sessions and requests, leave feedback |
| Admin Portal | `admin-dashboard.html` | Manage users, moderate listings, view platform statistics |

---

## Features

### Student Features
- **Register & Login** — Three-step registration with department, year of study, and student ID
- **Browse Skills** — Search and filter skill listings by category or keyword
- **My Skills** — Create, edit, pause, and delete your own skill listings
- **Session Requests** — Send, accept, decline, and withdraw session requests
- **Sessions** — View upcoming and past sessions; mark sessions as complete or cancel them
- **Feedback** — Leave star ratings and written reviews for completed sessions; reply to and report reviews
- **Profile** — Public profile showing average rating, sessions taught, and skills offered

### Admin Features
- **Dashboard** — Platform-wide KPIs: total users, active listings, completed sessions, reported feedback
- **User Management** — Search, suspend, reactivate, and delete student accounts; reset passwords
- **Skill Moderation** — View all listings, review flagged ones, clear flags or remove listings entirely
- **Session Overview** — View all sessions across the platform

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | HTML5, CSS3 (custom design system), Vanilla JavaScript |
| **Backend** | Java 17, Spring Boot 3.2 |
| **Database** | PostgreSQL 14+ |
| **Authentication** | JWT (JSON Web Tokens) via `jjwt 0.12.5` |
| **Security** | Spring Security 6, BCrypt (strength 12) |
| **ORM** | Spring Data JPA / Hibernate 6 |
| **Build Tool** | Apache Maven 3.8+ |

---

## Project Structure

```
SkillSwap/
│
├── frontend/                           # Static HTML/CSS/JS — no build step needed
│   ├── index.html                      # Public landing page
│   ├── login.html                      # Student login
│   ├── register.html                   # 3-step student registration
│   ├── dashboard.html                  # Student home dashboard
│   ├── profile.html                    # Student profile page
│   ├── my-skills.html                  # Manage own skill listings
│   ├── browse-skills.html              # Browse & search all skills
│   ├── sessions.html                   # Upcoming & completed sessions
│   ├── requests.html                   # Incoming & outgoing session requests
│   ├── feedback.html                   # Reviews received, given & pending
│   ├── admin-login.html                # Admin login portal
│   ├── admin-dashboard.html            # Admin overview & stats
│   ├── admin-users.html                # Admin user management
│   ├── admin-skills.html               # Admin skill moderation
│   ├── api.js                          # Shared API client (required on every page)
│   ├── dashboard-shell.css             # Shared styles for student pages
│   └── admin-shell.css                 # Shared styles for admin pages
│
└── skillswap/                          # Spring Boot backend (Maven project)
    ├── pom.xml
    └── src/main/
        ├── java/com/skillswap/
        │   ├── SkillSwapApplication.java
        │   │
        │   ├── config/
        │   │   ├── CorsConfig.java             # CORS — wildcard in dev, restrict for prod
        │   │   └── SecurityConfig.java         # Spring Security + JWT filter chain
        │   │
        │   ├── security/
        │   │   ├── JwtUtil.java                # Token generation & validation (HS256)
        │   │   ├── JwtAuthFilter.java          # Reads Bearer token on every request
        │   │   └── UserDetailsServiceImpl.java # Loads user from DB for Spring Security
        │   │
        │   ├── model/                          # JPA entities mapped to PostgreSQL tables
        │   │   ├── User.java
        │   │   ├── SkillListing.java
        │   │   ├── SessionRequest.java
        │   │   ├── Session.java
        │   │   └── Feedback.java
        │   │
        │   ├── repository/                     # Spring Data JPA repositories
        │   │   ├── UserRepository.java
        │   │   ├── SkillListingRepository.java
        │   │   ├── SessionRequestRepository.java
        │   │   ├── SessionRepository.java
        │   │   └── FeedbackRepository.java
        │   │
        │   ├── dto/                            # Request & response data transfer objects
        │   │   ├── ApiResponse.java            # Success<T> and Error wrappers
        │   │   ├── AuthDTO.java
        │   │   ├── UserDTO.java
        │   │   ├── SkillDTO.java
        │   │   ├── RequestDTO.java
        │   │   ├── SessionDTO.java
        │   │   └── FeedbackDTO.java
        │   │
        │   ├── service/                        # Business logic layer
        │   │   ├── AuthService.java
        │   │   ├── UserService.java
        │   │   ├── SkillService.java
        │   │   ├── RequestService.java         # Accepts request → auto-creates Session
        │   │   ├── SessionService.java
        │   │   ├── FeedbackService.java
        │   │   └── AdminService.java
        │   │
        │   ├── controller/                     # REST API controllers
        │   │   ├── BaseController.java         # Resolves User from JWT principal
        │   │   ├── AuthController.java         # /api/auth/**
        │   │   ├── UserController.java         # /api/users/**
        │   │   ├── SkillController.java        # /api/skills/**
        │   │   ├── RequestController.java      # /api/requests/**
        │   │   ├── SessionController.java      # /api/sessions/**
        │   │   ├── FeedbackController.java     # /api/feedback/**
        │   │   └── AdminController.java        # /api/admin/** (ADMIN role only)
        │   │
        │   └── exception/
        │       ├── AppException.java           # Typed runtime exceptions (400/403/404/409)
        │       └── GlobalExceptionHandler.java # @RestControllerAdvice — uniform JSON errors
        │
        └── resources/
            ├── application.yml                 # Database URL, JWT config, server port
            └── schema.sql                      # Full DDL, indexes, triggers, admin seed
```

---

## Getting Started

### Prerequisites

| Tool | Minimum Version | Download |
|------|----------------|----------|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.8 | https://maven.apache.org |
| PostgreSQL | 14 | https://www.postgresql.org/download |

---

### 1. Database Setup

Open a terminal and connect to PostgreSQL:

```bash
psql -U postgres
```

Create the database:

```sql
CREATE DATABASE skillswap;
\q
```

Run the schema script to create all tables, indexes, triggers, and the default admin account:

```bash
psql -U postgres -d skillswap -f skillswap/src/main/resources/schema.sql
```

> If you are re-running this after a previous attempt and the admin insert fails due to a duplicate email, run the following first:
> ```sql
> DELETE FROM users WHERE email = 'admin@skillswap.ac.ke';
> ```
> Then run `schema.sql` again.

---

### 2. Backend Configuration

Open `skillswap/src/main/resources/application.yml` and update your credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/skillswap
    username: postgres        # ← change if your PostgreSQL user is different
    password: postgres        # ← change to your actual PostgreSQL password
```

Everything else can stay as-is for local development. The server runs on port `8080` by default.

---

### 3. Running the Backend

**Command line (Maven):**

```bash
cd skillswap
mvn clean package -DskipTests
java -jar target/skillswap-1.0.0.jar
```

**Or with the Spring Boot plugin:**

```bash
cd skillswap
mvn spring-boot:run
```

**IntelliJ IDEA:**
1. Open the `skillswap/` folder as a Maven project
2. Wait for dependencies to import (bottom progress bar)
3. Open `SkillSwapApplication.java` and click the green ▶ button

**VS Code:**
1. Install the **Extension Pack for Java** from the marketplace
2. Open `skillswap/` as a folder
3. Open `SkillSwapApplication.java` and click **Run** above `main()`

The server is ready when you see:

```
Started SkillSwapApplication in X.XXX seconds
```

**Verify the backend is running:**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@skillswap.ac.ke","password":"Admin@1234"}'
```

A successful response returns a JSON object with a `token` field.

---

### 4. Running the Frontend

No build step is needed. The frontend is plain HTML.

**Option A — Open directly in browser:**

Double-click any `.html` file. CORS is configured to accept `file://` origins.

**Option B — VS Code Live Server (recommended for development):**

1. Install the **Live Server** extension
2. Right-click `dashboard.html` → **Open with Live Server**
3. Browser opens at `http://127.0.0.1:5500`

**Option C — Python or Node static server:**

```bash
# Python
cd frontend && python3 -m http.server 5500

# Node.js
cd frontend && npx serve -p 5500
```

Then navigate to `http://localhost:5500`.

> `api.js` must be loaded on every HTML page **before** any page script. It provides `Auth`, `UserAPI`, `SkillAPI`, and all other shared utilities.

---

## Default Accounts

### Admin Account

| Field | Value |
|-------|-------|
| Login page | `admin-login.html` |
| Email | `admin@skillswap.ac.ke` |
| Password | `Admin@1234` |
| Role | `ADMIN` |

> Change this password immediately after first login: Admin Panel → Users → find System Admin → Reset Password.

### Student Accounts

New student accounts are created through `register.html`. The three-step form collects:

| Step | Fields |
|------|--------|
| 1 — Account | First name, last name, email, password |
| 2 — Academic | Department, year of study, student ID |
| 3 — Skills | Skills you can teach, terms acceptance |

---

## API Reference

Base URL: `http://localhost:8080/api`

All protected endpoints require the header:
```
Authorization: Bearer <your_jwt_token>
```

All responses follow this envelope:
```json
{ "data": { ... } }
```

Errors return:
```json
{ "message": "Human-readable error", "status": 400 }
```

---

### Auth — `/api/auth` _(public)_

| Method | Path | Body | Description |
|--------|------|------|-------------|
| `POST` | `/auth/register` | `{ firstName, lastName, email, password, department, yearOfStudy, studentId, bio }` | Create a student account |
| `POST` | `/auth/login` | `{ email, password }` | Login — returns token + user info |

---

### Users — `/api/users`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/users/me` | Current user's full profile |
| `PUT` | `/users/me` | Update profile fields |
| `GET` | `/users/{id}` | Any user's public profile |

---

### Skills — `/api/skills`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/skills/listings` | Browse active listings (`?category=&search=`) |
| `GET` | `/skills/listings/{id}` | Single listing |
| `GET` | `/skills/my` | Current user's listings |
| `POST` | `/skills` | Create listing |
| `PUT` | `/skills/{id}` | Update listing |
| `DELETE` | `/skills/{id}` | Delete listing |
| `PATCH` | `/skills/{id}/toggle` | Pause / unpause |
| `POST` | `/skills/{id}/flag?reason=` | Flag for admin review |

Valid **categories:** `PROGRAMMING` `DESIGN` `MATHEMATICS` `LANGUAGES` `BUSINESS` `SCIENCE` `ARTS` `OTHER`

Valid **levels:** `BEGINNER` `INTERMEDIATE` `ADVANCED`

---

### Requests — `/api/requests`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/requests` | Send a session request |
| `GET` | `/requests/incoming` | Requests addressed to me (I am teacher) |
| `GET` | `/requests/outgoing` | Requests I sent (I am learner) |
| `PATCH` | `/requests/{id}/respond` | Accept or decline |
| `PATCH` | `/requests/{id}/withdraw` | Withdraw a pending request |

Respond body: `{ "action": "ACCEPTED" }` or `{ "action": "DECLINED", "declineReason": "..." }`

> Accepting automatically creates a confirmed `Session`.

---

### Sessions — `/api/sessions`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/sessions` | All my sessions |
| `GET` | `/sessions/upcoming` | Future confirmed sessions |
| `GET` | `/sessions/history` | Completed sessions |
| `GET` | `/sessions/{id}` | Single session |
| `PATCH` | `/sessions/{id}/cancel` | Cancel a confirmed session |
| `PATCH` | `/sessions/{id}/complete` | Mark as completed |

Each response includes `myRole` (`"TEACHER"` or `"LEARNER"`) and `feedbackGiven` (`true` / `false`).

---

### Feedback — `/api/feedback`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/feedback` | Leave feedback for a completed session |
| `GET` | `/feedback/received` | Feedback I received |
| `GET` | `/feedback/given` | Feedback I gave |
| `GET` | `/feedback/user/{id}` | Public feedback for any user |
| `PATCH` | `/feedback/{id}/reply?text=` | Reply to a review (reviewee only) |
| `PATCH` | `/feedback/{id}/report` | Report inappropriate feedback |

---

### Admin — `/api/admin` _(ADMIN role required)_

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/admin/stats` | Platform KPIs |
| `GET` | `/admin/users` | All users |
| `GET` | `/admin/users/search?query=` | Search users |
| `GET` | `/admin/users/{id}` | Single user |
| `PATCH` | `/admin/users/{id}/suspend` | Suspend account |
| `PATCH` | `/admin/users/{id}/activate` | Reactivate account |
| `DELETE` | `/admin/users/{id}` | Delete account permanently |
| `PATCH` | `/admin/users/{id}/reset-password` | Reset password |
| `GET` | `/admin/listings` | All skill listings |
| `GET` | `/admin/listings/flagged` | Flagged listings |
| `PATCH` | `/admin/listings/{id}/clear-flag` | Clear a flag |
| `DELETE` | `/admin/listings/{id}` | Remove a listing |
| `GET` | `/admin/sessions` | All sessions on the platform |
| `GET` | `/admin/feedback/reported` | All reported feedback |

---

## Database Schema

Five tables. Enums are stored as `VARCHAR` with `CHECK` constraints, compatible with JPA `@Enumerated(STRING)`.

```
users
  id · email (unique) · password_hash
  first_name · last_name · department · year_of_study · student_id · bio
  role: STUDENT | ADMIN
  status: ACTIVE | SUSPENDED
  average_rating · total_sessions · created_at · updated_at

skill_listings
  id · user_id → users
  title · description
  category: PROGRAMMING | DESIGN | MATHEMATICS | LANGUAGES | BUSINESS | SCIENCE | ARTS | OTHER
  level: BEGINNER | INTERMEDIATE | ADVANCED
  session_duration · availability · is_active · is_flagged · flag_reason
  average_rating · total_sessions · created_at · updated_at

session_requests
  id · skill_listing_id → skill_listings · requester_id → users · teacher_id → users
  status: PENDING | ACCEPTED | DECLINED | WITHDRAWN
  proposed_datetime · duration · focus_message
  meeting_format: IN_PERSON | ONLINE_GOOGLE_MEET | ONLINE_ZOOM | FLEXIBLE
  decline_reason · created_at · updated_at

sessions
  id · request_id → session_requests (unique) · skill_listing_id · teacher_id · learner_id
  scheduled_at · duration · meeting_format
  status: CONFIRMED | COMPLETED | CANCELLED
  cancel_reason · completed_at · created_at · updated_at

feedback
  id · session_id → sessions · reviewer_id → users · reviewee_id → users · skill_listing_id
  overall_rating (1–5) · review_text · teacher_reply · is_reported
  created_at · updated_at
  UNIQUE (session_id, reviewer_id)   ← one review per session per person
```

**PostgreSQL triggers (automatic, no application code needed):**

| Trigger | Table | Action |
|---------|-------|--------|
| `set_updated_at` | All 5 tables | Sets `updated_at = NOW()` on every `UPDATE` |
| `recalc_listing_rating` | `feedback` | Recalculates `skill_listings.average_rating` after insert / update / delete |
| `recalc_user_rating` | `feedback` | Recalculates `users.average_rating` (teacher side) after insert / update / delete |
| `on_session_completed` | `sessions` | Increments `total_sessions` on the listing and both users when status changes to `COMPLETED` |

---

## How It Works

The complete journey from listing to review:

```
1.  Student A creates a skill listing        →  POST  /api/skills
2.  Student B finds it while browsing        →  GET   /api/skills/listings
3.  Student B sends a session request        →  POST  /api/requests
4.  Student A sees it in incoming requests   →  GET   /api/requests/incoming
5.  Student A accepts                        →  PATCH /api/requests/{id}/respond
        └─ A Session (CONFIRMED) is auto-created in the same transaction
6.  (Optional) Either student cancels        →  PATCH /api/sessions/{id}/cancel
7.  Either student marks it complete         →  PATCH /api/sessions/{id}/complete
        └─ DB trigger increments total_sessions on the listing and both users
8.  Both students leave reviews              →  POST  /api/feedback
        └─ DB trigger recalculates average_rating on listing and teacher
9.  Teacher replies to their review          →  PATCH /api/feedback/{id}/reply
```

---

## Security

| Concern | Implementation |
|---------|---------------|
| Password storage | BCrypt, strength 12 |
| Session tokens | JWT signed with HS256, 24-hour expiry |
| Auth enforcement | `JwtAuthFilter` on every request; `/api/auth/**` is the only public path |
| Admin-only routes | `@PreAuthorize("hasRole('ADMIN')")` on all `/api/admin/**` methods |
| Self-request guard | Service-layer check — you cannot request your own skill listing |
| Duplicate request guard | DB + service — one pending request per skill per requester |
| Self-review guard | DB `CHECK` constraint + service validation |
| Input sanitisation | HTML tags stripped in `AuthService` and `UserService` |
| CORS | `allowedOriginPatterns("*")` for local development |

> **Before going to production:** replace `"*"` in `CorsConfig.java` with your actual frontend domain, and generate a fresh random JWT secret in `application.yml`.

---

## Troubleshooting

### Admin login fails with "incorrect password"

The seed hash in an earlier version of `schema.sql` was incorrect. Fix the admin password directly in PostgreSQL:

```sql
psql -U postgres -d skillswap

DELETE FROM users WHERE email = 'admin@skillswap.ac.ke';
INSERT INTO users (email, password_hash, first_name, last_name, role, status)
VALUES (
  'admin@skillswap.ac.ke',
  '$2a$12$6C5ssrwUYP8811ki/eY9pOdYzJ3z/HWCl7p0696Z967Z7gfz0RKgu',
  'System', 'Admin', 'ADMIN', 'ACTIVE'
);
```

Password is `Admin@1234`.

---

### "Could not load data" on every page

The frontend cannot reach the backend. Diagnose in this order:

1. Confirm Spring Boot is running — terminal should show `Started SkillSwapApplication`.
2. Test the backend directly:
   ```bash
   curl http://localhost:8080/api/auth/login \
     -X POST -H "Content-Type: application/json" \
     -d '{"email":"x","password":"x"}'
   ```
   You should get a JSON error response — not a connection refused.
3. Open browser DevTools → **Network** tab → reload the page and inspect the failed request. Note the HTTP status code:
   - No response / connection refused → backend is not running
   - `401` → token is missing or expired (check Local Storage for `ss_token`)
   - `403` → hitting an admin route as a student, or vice versa
   - `404` → the endpoint path is wrong
   - `500` → server-side error (check Spring Boot terminal logs)

---

### `relation "users" does not exist` on startup

The schema has not been applied. Run:

```bash
psql -U postgres -d skillswap -f skillswap/src/main/resources/schema.sql
```

---

### `SchemaManagementException: Schema-validation` on startup

Hibernate validates the DB schema against the entities at startup (`ddl-auto: validate`). A mismatch means you are running an old database. Drop and recreate:

```sql
DROP DATABASE skillswap;
CREATE DATABASE skillswap;
```

Then re-run `schema.sql`.

---

### `password authentication failed for user "postgres"`

Your PostgreSQL installation uses a different password. Update `application.yml`:

```yaml
spring:
  datasource:
    password: your_real_password_here
```

---

### CORS error in the browser console

Confirm `CorsConfig.java` contains:

```java
config.setAllowedOriginPatterns(List.of("*"));
config.setAllowCredentials(true);
```

Then **restart** the Spring Boot server (config changes require a restart).

---

### Pages still show "Amara Kofi" / dummy data after login

`api.js` is either not loaded on that page, or is loaded after the page script. Check that every HTML page has:

```html
<script src="api.js"></script>
<script>
  /* your page script here */
</script>
```

in that order, inside `</body>` — not after `</html>`.

---

*SkillSwap — Built with Java 17 · Spring Boot 3.2 · PostgreSQL · Vanilla JS*
*Technical University of Kenya· Information Tech · Section Black*
