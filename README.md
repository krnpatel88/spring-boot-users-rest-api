# Users REST API — Spring Boot Learning Project

A simple Spring Boot REST API for managing `User` records (Create, Read, Delete), built as a
learning reference for Spring Boot fundamentals: layered architecture (Controller → Service →
Repository), dependency injection, and unit/integration testing.

This project intentionally uses an **in-memory store** (no database) to keep the focus on REST
API structure and testing, rather than persistence.

---

## Tech Stack

| Component        | Version / Notes                                    |
|-------------------|-----------------------------------------------------|
| Java              | 17+                                                  |
| Spring Boot       | 4.x (uses **Jackson 3**, not Jackson 2 — see notes below) |
| Build tool        | Maven (via Maven Wrapper — no local Maven install needed) |
| Testing           | JUnit 5, Mockito, MockMvc                            |

> **Note on Spring Boot 4 / Jackson 3:** This project was built against Spring Boot 4, which
> ships with Jackson 3 by default. Some class packages differ from the more commonly documented
> Spring Boot 3.x / Jackson 2.x tutorials online — for example, `ObjectMapper` now lives under
> `tools.jackson.databind` instead of `com.fasterxml.jackson.databind`, and `@MockBean` has been
> replaced by `@MockitoBean`. If you're following older tutorials alongside this repo, keep this
> in mind.

---

## Project Structure

```
src/main/java/com/example/demo/
    DemoApplication.java     # Spring Boot entry point
    User.java                 # Model (POJO)
    UserRepository.java       # In-memory data layer
    UserService.java          # Business logic layer
    UserController.java       # REST endpoints (HTTP layer)

src/test/java/com/example/demo/
    UserRepositoryTest.java   # Unit tests — data layer
    UserServiceTest.java      # Unit tests — business logic (mocked repository)
    UserControllerTest.java   # Slice tests — HTTP layer (mocked service)

pom.xml                       # Maven build config (dependencies, plugins)
mvnw / mvnw.cmd                # Maven Wrapper — run builds without installing Maven
```

For setup, installation, and run instructions, see **[SETUP.md](./SETUP.md)**.

---

## API Endpoints

Base path: `/api/users`

| Method | Endpoint            | Description         | Request Body                          | Success Response |
|--------|----------------------|----------------------|-----------------------------------------|-------------------|
| GET    | `/api/users`         | Get all users         | —                                        | `200 OK`          |
| GET    | `/api/users/{id}`    | Get a user by ID      | —                                        | `200 OK`          |
| POST   | `/api/users`         | Create a new user     | `{ "name": "...", "email": "..." }`     | `201 Created`     |
| DELETE | `/api/users/{id}`    | Delete a user by ID   | —                                        | `204 No Content`  |

A `GET`/`DELETE` for a non-existent `id` returns `404 Not Found`.

### Example requests (curl)

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'

# Get all users
curl http://localhost:8080/api/users

# Get a single user
curl http://localhost:8080/api/users/1

# Delete a user
curl -X DELETE http://localhost:8080/api/users/1
```

---

## Architecture Overview

This project follows a standard **layered (package-by-layer) architecture**:

```
HTTP request
    │
    ▼
UserController   →  handles routing, HTTP status codes, request/response bodies
    │
    ▼
UserService      →  business logic, validation, orchestration
    │
    ▼
UserRepository   →  data access (in-memory Map here; would be a database in a real app)
```

Each layer only talks to the one directly below it, which keeps responsibilities separated and
makes each layer independently testable (see [Testing](#testing) below).

---

## Testing

The project includes tests at three levels, following the standard testing pyramid:

| Layer               | Test file                  | What it verifies                                  | Spring context loaded? |
|----------------------|------------------------------|-----------------------------------------------------|--------------------------|
| Repository (unit)     | `UserRepositoryTest.java`   | In-memory CRUD logic in isolation                    | No                        |
| Service (unit)        | `UserServiceTest.java`      | Business logic, with repository mocked (Mockito)     | No                        |
| Controller (slice)    | `UserControllerTest.java`   | HTTP routing/status codes, with service mocked        | Partial (`@WebMvcTest`)   |

Run all tests with:
```bash
./mvnw test
```
See [SETUP.md](./SETUP.md#running-tests) for more options (running a single test, generating
reports, etc.).

---

## Contributing / Learning Notes

This repo is meant as a teaching reference. If you're using it to learn Spring Boot:

- Start with `User.java` (plain data model) → `UserRepository.java` (data access) →
  `UserService.java` (business logic) → `UserController.java` (HTTP layer). This mirrors the
  order a request flows through the app, just in reverse.
- Read the tests alongside each class — they show the intended behavior and edge cases
  (e.g., what happens when a user isn't found).
- Try extending it: add an `updateUser` (PUT) endpoint, add field validation, or swap the
  in-memory `UserRepository` for a real database using Spring Data JPA.

---

## License

This is a sample/learning project. Feel free to fork, modify, and reuse for your own learning.
