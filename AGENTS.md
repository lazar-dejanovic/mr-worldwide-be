# AGENTS.md — MR Worldwide Backend

AI coding agent guide for this Spring Boot 3 / Java 21 travel-planner API.

## Architecture Overview

**MR Worldwide** is a REST API for AI-assisted multi-destination trip planning. Users create `TripPlan`s composed of ordered `TripSegment`s (destination hops), each with an optional `Accommodation` and `Transport`, plus timestamped `DailyItinerary` activities.

```
TripPlan → [TripSegment (ordered by orderIndex)] → Transport (AIRPLANE | TRAIN | VEHICLE)
                                                  → Accommodation
                                                  → [DailyItinerary]
```

Key package layout:
- `domain/entities/` — JPA entities grouped by subdomain (`trip/`, `user/`, `accomodation/`, `transport/`)
- `domain/dto/` — Java records used as API payloads (never expose entities directly)
- `domain/mappers/` — MapStruct mappers (singleton via `Mapper.INSTANCE`)
- `dao/repositories/` — Spring Data JPA repositories (flat, no subpackages)
- `services/trip/` and `services/ums/` — business logic split by subdomain
- `web/controllers/user/` — `@RestController` classes (all controllers live here despite the `user/` path)
- `security/` — stateless JWT filter (`AuthenticationFilter`) + request/response POJOs
- `config/` — all Spring configuration beans

## Developer Workflows

**Run locally (requires PostgreSQL on `localhost:5432/mr-worldwide`):**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Build fat JAR:**
```bash
./gradlew bootJar   # outputs build/libs/mr-worldwide.jar
```

**Unit tests:**
```bash
./gradlew test
```

**Integration tests** (separate source set `src/test-integration/java`):
```bash
./gradlew integrationTest
```

**Scheduling is disabled by default** (`app.scheduling.enable=false`). Enable via `application-local.properties` or with `--app.scheduling.enable=true`. Scheduler uses ShedLock (JDBC) for distributed locking.

## Entity & DTO Conventions

- All entities extend `BaseEntityUUID` (UUID PK + `@Version` for optimistic locking) → `BaseEntityAudit` (`createdOn`, `updatedOn`, `createdBy`, `lastModifiedBy` auto-populated via `SpringSecurityAuditorAware`).
- `List<String>` fields stored as CSV text columns use `@Convert(converter = CsvConverter.class)` — see `TripPlan.destinations`, `TripPlan.interests`, `UserTripPreference.hobbies`.
- DTOs are **Java records**. The `id()` convenience method reads from the nested `BaseEntityDto base` field (e.g., `TripPlanDto.id()`, `UserDto.id()`).
- MapStruct mappers declare `@Mapping(target = "base", source = "entity")` and implement a `default toBase(Entity)` method to populate `BaseEntityDto`.
- Use `unmappedTargetPolicy = ReportingPolicy.IGNORE` and `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE` on every `@Mapper`.

## Authentication & Authorization

- Stateless JWT (HS512, 30-day expiry). Token is issued at login inside `UserDto.accessToken`.
- All requests except `POST /api/users/login`, `POST /api/users/register`, `/api/users/forgot-password`, `/api/users/reset-password`, OPTIONS, and actuator endpoints require `Authorization: Bearer <token>`.
- The authenticated `User` entity is stored directly as the principal. Retrieve it anywhere via `AuthUtils.getLoggedUser()`.
- Ownership check pattern (used in `TripPlanService`): compare `user.getEmail()` with `entity.getCreatedBy()` (populated by JPA auditing).

## Exception Handling

All custom exceptions extend `HttpResponseException` and are annotated with `@ResponseStatus`. The `AuthenticationFilter` catches `HttpResponseException` subclasses and writes the status code + message directly to the response. Add new domain exceptions as subclasses of `HttpResponseException` with the appropriate `@ResponseStatus`.

Existing types: `NotFoundException` (404), `BadRequestException` (400), `ForbiddenException` (403), `AuthorizationException` (401), `ValidationException`, `InternalServerError`.

## Key Configuration Notes

- **Database**: PostgreSQL only in `application-local.properties`; `spring.jpa.hibernate.ddl-auto=create-drop` locally (schema is recreated on each start). Liquibase is present but disabled (`spring.liquibase.enabled=false`).
- **Hibernate UUID strategy**: `spring.jpa.properties.hibernate.id.db_structure_naming_strategy=legacy` is required — do not remove.
- **Caching**: Caffeine cache is enabled via `CacheConfig`; use Spring `@Cacheable` annotations where needed.
- **Async**: `@Async` tasks run on the `AsyncJobExecutor-` thread pool (core 10, queue 500).
- **WebClient** bean has a 10 MB in-memory buffer — use it for external HTTP calls.

## Adding a New Feature (Typical Flow)

1. Add entity in `domain/entities/<subdomain>/`, extending `BaseEntityUUID`.
2. Add repository in `dao/repositories/`.
3. Add DTO record in `domain/dto/<subdomain>/` with a `BaseEntityDto base` field.
4. Add/update MapStruct mapper in `domain/mappers/`.
5. Implement service in `services/<subdomain>/` annotated `@Transactional(readOnly = true)`; override with `@Transactional` on write methods.
6. Add controller in `web/controllers/user/` mapping under `/api/`.

