# Game Platform Demo

A REST API backend for a digital game distribution platform. Provides user authentication, a games catalog, purchase processing, and a personal game library.

## Tech Stack

- **Java 21** + **Spring Boot 4.0.5**
- **PostgreSQL 16** with **Flyway** schema migrations
- **Spring Security** — JWT access tokens (HS256, 15 min) + HttpOnly refresh token cookies (14 days)
- **Argon2** password hashing
- **Docker / Docker Compose**
- **Springdoc OpenAPI 3** — Swagger UI at `/swagger-ui.html`

---

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development without Docker)

---

## Running with Docker Compose

Copy `.env.example` to `.env` and fill in your values (see [Environment Variables](#environment-variables)), then:

```bash
docker compose up --build
```

The API will be available at `http://localhost:9090`.

To stop and remove containers:

```bash
docker compose down
```

To also remove the database volume:

```bash
docker compose down -v
```

---

## Local Development

The `bootRun` task reads variables from a `.env` file in the project root automatically.

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8082`.

**Database:** The app expects a running PostgreSQL instance. The easiest way is to start only the database container:

```bash
docker compose up db
```

---

## Environment Variables

Create a `.env` file in the project root with the following variables:

| Variable             | Description                                              | Example                  |
|----------------------|----------------------------------------------------------|--------------------------|
| `POSTGRES_HOST`      | PostgreSQL hostname (use `host.docker.internal` in Docker, `localhost` locally) | `localhost` |
| `POSTGRES_PORT`      | PostgreSQL port                                          | `5432`                   |
| `POSTGRES_DB`        | Database name                                            | `game-platform-db`       |
| `POSTGRES_USER`      | Database user                                            | `devuser`                |
| `POSTGRES_PASSWORD`  | Database password                                        | `devpass`                |
| `SECRET_KEY`         | JWT signing secret — **must be 32+ characters**          | *(generate a random string)* |

**CORS:** By default the API allows requests from `http://localhost:3000`. Override for a different frontend origin:

```properties
cors.allowed-origin=https://yourfrontend.com
```

Set this as an environment variable or add it to your deployment configuration.

---

## Running Tests

Integration tests use [Testcontainers](https://testcontainers.com/) and require Docker to be running.

```bash
./gradlew test
```

Unit and service-level tests (no Docker required) run alongside the integration tests. If Docker is not available, integration tests are skipped automatically.

---

## API Reference

Interactive documentation is available at `/swagger-ui.html` when the app is running.

### Authentication

All protected endpoints require an `Authorization: Bearer <token>` header.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/API/auth/login` | Public | Log in. Returns a JWT access token and sets an HttpOnly refresh cookie. |
| `POST` | `/API/auth/refresh` | Cookie | Exchange a refresh cookie for a new access token (rotates the cookie). |
| `POST` | `/API/auth/logout` | Cookie | Revoke the current refresh token. |

**Login request:**
```json
{ "username": "alice", "password": "s3cr3t" }
```

**Login response:**
```json
{ "token": "<jwt>", "tokenType": "Bearer", "expiresInSeconds": 900 }
```

---

### Users

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/API/users` | Public | Register a new user. Returns 201 with a `Location` header. |
| `GET` | `/API/users/me` | Required | Get the currently authenticated user's profile. |
| `GET` | `/API/users` | Required | List all users. |
| `GET` | `/API/users/{id}` | Required | Get a user by ID. |
| `GET` | `/API/users/username/{username}` | Required | Get a user by username. |
| `GET` | `/API/users/email/{email}` | Required | Get a user by email address. |
| `GET` | `/API/users/{id}/details` | Required | Get a user with their emails and addresses. |
| `GET` | `/API/users/{id}/emails` | Required | Get a user with their email addresses. |

**Register request:**
```json
{ "username": "alice", "password": "s3cr3t", "birthDate": "1990-01-15" }
```

---

### Games

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/API/games` | Public | List all games. |
| `GET` | `/API/games/{id}` | Public | Get a game by ID. |
| `GET` | `/API/games/name/{name}` | Public | Get a game by name. |
| `POST` | `/API/games` | Required | Create a new game. |
| `PUT` | `/API/games/{id}/genres` | Required | Set the genres for a game. |

---

### Purchases

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/API/games/{gameId}/purchase` | Required | Purchase a game. Returns 201 with the purchase record. |
| `POST` | `/API/purchases/{purchaseId}/refund` | Required | Refund a purchase. |
| `GET` | `/API/users/{userId}/purchases` | Required | Get a user's purchase history. |

Duplicate purchases of a non-refunded game are rejected with `409 Conflict`. After a refund, the game can be re-purchased.

---

### Library

The library is populated automatically when a game is purchased and removed when a purchase is refunded (managed by database triggers).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/API/me/library` | Required | Get the current user's game library. |
| `PATCH` | `/API/me/library/{gameId}` | Required | Update playtime or installed status for a library entry. |

**Update request (all fields optional):**
```json
{ "totalPlaytimeMinutes": 120, "isInstalled": true }
```

---

### Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/API/health` | Public | Spring Boot Actuator health check. |

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-06-10T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Game not found: 99",
  "path": "/API/games/99"
}
```

| Status | Meaning |
|--------|---------|
| `400` | Validation error or malformed request body |
| `401` | Missing or invalid JWT |
| `404` | Resource not found |
| `409` | Conflict (e.g. duplicate purchase, username already taken) |
| `500` | Unexpected server error |

---

## Frontend Integration

Configure your frontend with:

- **Base URL:** `http://localhost:9090` (Docker) or `http://localhost:8082` (local)
- **Auth header:** `Authorization: Bearer <token>` on all protected requests
- **Refresh flow:** Send a `POST /API/auth/refresh` — the browser will include the HttpOnly refresh cookie automatically. No manual cookie handling needed.
- **CORS origin:** Set `cors.allowed-origin` to match your frontend's URL if it differs from `http://localhost:3000`
