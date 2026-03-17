# MR Worldwide — Backend

REST API for AI-assisted multi-destination trip planning, built with **Spring Boot 3.5 / Java 21**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Persistence | Spring Data JPA + PostgreSQL |
| Security | Stateless JWT (HS512, jjwt 0.12) |
| Mapping | MapStruct 1.5.5 |
| Boilerplate | Lombok 1.18 |
| HTTP client | Spring WebFlux `WebClient` |
| Caching | Caffeine (Spring Cache) |
| Scheduling | Spring Scheduling + ShedLock (JDBC) |
| Build | Gradle 8 |
| API docs | SpringDoc OpenAPI (Swagger UI at `/swagger-ui/index.html`) |

---

## Prerequisites

- **Java 21**
- **Docker & Docker Compose** (recommended) — or a local PostgreSQL 15+ instance

---

## Running Locally

### Option A — Docker Compose (recommended)

Starts PostgreSQL, pgAdmin, and the API together:

```bash
docker compose -f docker-compose.local.yml up --build
```

| Service | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| pgAdmin | http://localhost:5050 (admin@mr-worldwide.dev / admin) |

### Option B — Gradle + external PostgreSQL

1. Create a database named `mr-worldwide` on `localhost:5432` with user `postgres`.
2. Start the app:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

The `local` profile uses `create-drop` DDL — the schema is rebuilt on every start.

---

## Build

```bash
# Fat JAR → build/libs/mr-worldwide.jar
./gradlew bootJar

# Docker image
docker build -t mr-worldwide .
```

---

## Tests

```bash
# Unit tests
./gradlew test

# Integration tests (Testcontainers — needs Docker)
./gradlew integrationTest
```

---

## Project Structure

```
src/main/java/com/raf/mrworldwide/
├── config/          # Spring beans (security, async, cache, JPA auditing, WebClient)
├── dao/
│   └── repositories/  # Spring Data JPA repositories (flat, no subpackages)
├── domain/
│   ├── converters/  # CsvConverter — List<String> ↔ comma-separated TEXT column
│   ├── dto/         # Java records (API payloads — never expose entities directly)
│   ├── entities/    # JPA entities grouped by subdomain (trip/, user/, accomodation/, transport/)
│   └── mappers/     # MapStruct mappers (accessed via Mapper.INSTANCE singleton)
├── exceptions/      # HttpResponseException hierarchy
├── security/        # AuthenticationFilter (JWT) + request POJOs
├── services/
│   ├── trip/        # TripPlanService
│   └── ums/         # AuthService, UserService, TokenAuthenticationService
├── utils/           # AuthUtils.getLoggedUser()
└── web/
    └── controllers/user/  # All @RestController classes
```

---

## Domain Model

```
TripPlan ──┬──► TripSegment (ordered by orderIndex)
           │         ├──► Transport
           │         │       ├──► AirplaneTransport  (AIRPLANE)
           │         │       └──► VehicleTransport   (VEHICLE / TRAIN)
           │         ├──► Accommodation
           │         └──► DailyItinerary[]
           └──► User ──► UserTripPreference
```

**TripPlan statuses:** `DRAFT → PLANNED → BOOKED → COMPLETED`

---

## API Endpoints

### Authentication (`/api/users`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/users/register` | Public | Register a new user |
| `POST` | `/api/users/login` | Public | Login — returns JWT in `accessToken` |
| `GET` | `/api/users/me` | Bearer | Current user profile |
| `GET` | `/api/users/{id}` | Bearer | Get user by ID |
| `PUT` | `/api/users/{id}` | Bearer | Update user |
| `POST` | `/api/users/forgot-password` | Public | Request password reset |
| `POST` | `/api/users/reset-password` | Public | Confirm password reset |

### Trip Plans (`/api/trips`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/trips` | Bearer | List all trip plans for the logged-in user |
| `GET` | `/api/trips/{id}` | Bearer | Get a specific trip plan (owner only) |

---

## Authentication

All protected endpoints require:

```
Authorization: Bearer <token>
```

The token is returned in the `accessToken` field of the login response. Tokens are valid for **30 days**.

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.jpa.hibernate.ddl-auto` | — | Set to `create-drop` in `local` profile |
| `spring.jpa.properties.hibernate.id.db_structure_naming_strategy` | `legacy` | Required for UUID PK generation — do not remove |
| `app.scheduling.enable` | `false` | Enable/disable scheduled tasks and ShedLock |
| `spring.liquibase.enabled` | `false` | Liquibase migrations (disabled, DDL managed by Hibernate locally) |

