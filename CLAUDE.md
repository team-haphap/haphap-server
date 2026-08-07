# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

합합 (Haphap) is a job-application tracking/notification platform. This repo is the Spring Boot backend: it manages job postings, hiring-stage results, Kakao OAuth login, FCM push notifications, search, and a calendar view. Java 21, Spring Boot 3.5.9, Gradle, PostgreSQL 16 + Flyway, Redis 7.

## Commands

```bash
./gradlew build              # compile + run tests + assemble jar
./gradlew bootRun             # run the app (needs a profile, see below)
./gradlew test                # run tests (src/test is currently empty — no test suite exists yet)
./gradlew test --tests "org.sopt.haphap.SomeTest"   # run a single test class
```

Local run requires the `local` profile and a Postgres/Redis instance (`docker-compose.yml` provides postgres+redis+dozzle):

```bash
docker compose up -d postgres redis
./gradlew bootRun --args='--spring.profiles.active=local'
```

`application-local.yml` is gitignored (contains local secrets) — copy/create it from `application.yml` + the `local`-specific keys referenced below if it's missing. `application-prod.yml` is used inside the Docker image (`Dockerfile` hardcodes `-Dspring.profiles.active=prod`) and pulls everything from env vars (`RDS_ENDPOINT`, `RDS_USERNAME`, `RDS_PASSWORD`, `JWT_SECRET`, `AWS_S3_BUCKET`, `FIREBASE_CONFIG_PATH`, admin credentials).

There is no linter/formatter configured in `build.gradle`.

## Architecture

### Package layout

Code is organized by domain, each following the same internal shape:

```
domain/<name>/
  controller/   REST controllers + a matching "<Name>ApiDocs" interface
  dto/          request/response records (dto/request, dto/response for larger domains)
  domain/ or entity/   JPA entities (naming is inconsistent across domains — some use `domain`, `user`/`admin` use `entity`)
  repository/   Spring Data JPA repositories
  service/      business logic
  code/         domain-scoped SuccessCode/ErrorCode enums
```

Domains: `user` (Kakao login, JWT, profile), `posting` (job postings, companies, categories, stages, stats), `registration` (applications + hiring-stage results), `alram` (notification settings + FCM dispatch), `calendar`, `search`, `banner`, `admin`.

`global/` holds cross-cutting concerns: JWT (`global/jwt`), Spring Security (`global/config/SecurityConfig`), the common API response envelope, global exception handling, S3/webp image upload, and Firebase/FCM config.

**Controller/Docs split**: every controller (`XController`) implements a matching `XApiDocs` interface that carries the springdoc/Swagger `@Operation`/`@ApiResponse` annotations. Keep annotations in the `*ApiDocs` interface, not on the controller method itself.

### Response & error handling convention

All API responses go through a single envelope defined by `ApiResponse` (`global/dto/ApiResponse.java`):
- `ApiResponse.success(successCode, data)` → `SuccessResponse<T>`
- `ApiResponse.failure(errorCode)` → `FailureResponse`

Success/error codes are enums implementing `SuccessResultCode`/`ErrorResultCode` (which extend `ResultCode`), one enum per domain (e.g. `AuthSuccessCode`, `AlramErrorCode`), plus `GlobalErrorCode` for cross-cutting errors. Business-logic failures are raised as `new CustomException(someErrorCode)` and caught centrally by `GlobalExceptionHandler` (`global/exception/GlobalExceptionHandler.java`), which also handles framework-level exceptions (validation, malformed JSON, missing params, method-not-allowed, etc.) and maps them to `GlobalErrorCode`. When adding a new failure mode, add an enum constant to the relevant `*ErrorCode`, not a new exception class.

Note: Spring Security exceptions must NOT be caught by the generic `Exception.class` handler in `GlobalExceptionHandler` — it explicitly re-throws anything from `org.springframework.security.*` so `ExceptionTranslationFilter` can still produce 401/403 via the handlers configured in `SecurityConfig`.

### Auth

Stateless JWT auth (`global/jwt`): `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`. Roles are `USER`/`ADMIN` (`global/jwt/Role.java`). `SecurityConfig` whitelists health check, Swagger, `/api/v1/auth/**`, `/api/v1/search/**`, `/api/v1/banners/**`, `/api/v1/admin/auth/**`, and public GET/PATCH endpoints on postings (view/card-click tracking); `/api/v1/admin/**` requires `ROLE_ADMIN`, everything else requires `ROLE_USER`. Login is Kakao OAuth only (`domain/user/service/AuthService`, `global/client` for the Kakao API client).

### Notification pipeline (event-driven, async, outside the write transaction)

Registering a hiring-stage result triggers a push notification through a deliberate transaction boundary split:

1. `registration` domain publishes `RegistrationCreatedEvent` after saving.
2. `alram/event/RegistrationEventListener` is a `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` — it only fires after the DB write commits.
3. `AlramService.prepareAlrams(event)` runs in its own transaction to build an `AlramDispatch` (recipients + message).
4. `AlramDispatcher.dispatch(...)` runs **outside** any transaction and sends per-target via `NotificationSender`, handling `InvalidTokenException` (deactivates the token), `RetryableNotificationException`/`NotificationDeliveryException` (records via `AlramFailureRecorder`), and a catch-all so one bad token never aborts the loop.

`NotificationSender` has multiple implementations (`FcmNotificationSender`, `LoggingNotificationSender`, `FailingNotificationSender`) — check which is wired for the active profile before assuming FCM is actually being hit.

### Posting stats aggregation

`domain/posting/service/aggregate/` maintains denormalized stage-result counts (`StageResultCountUpdater` for incremental updates, `StageResultCountReconciler`/`StageResultCountRebuilder` for correcting drift, `ViewCountCleanupScheduler` for scheduled cleanup). `domain/posting/service/calculator/` derives a posting's current/next hiring stage (`CurrentStageResolver`, `NextStageCalculator`) from registration data. `domain/posting/service/support/` assembles the composite `PostingAggregate` view model used across listing/detail/today-statistic services — prefer reusing `PostingAggregateLoader`/`PostingResponseAssembler` rather than re-querying when adding new posting-related endpoints.

### Database

Flyway migrations live in `src/main/resources/db/migration` (`V1__init_schema.sql` ... `V29__...`), `baseline-on-migrate: true`, `baseline-version: 1`. `ddl-auto` is always `validate` — schema changes must go through a new Flyway migration, never through Hibernate auto-DDL. One-off SQL scripts that are *not* migrations live in `src/main/resources/sql/`.

### Deployment

Docker blue-green deploy: GitHub Actions (`.github/workflows/deploy.yml`) builds the jar, builds/pushes a Docker image on push to `main`, then SSHes into the EC2 host and runs `scripts/deploy.sh`, which starts the idle color (blue/green) on the alternate port, waits for `/actuator/health` to pass, flips the Nginx upstream port, then tears down the old container. Firebase service-account JSON and other secrets are written to the host from GitHub secrets at deploy time — never commit them (already covered by `.gitignore`).
