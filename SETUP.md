# Setup Guide

Step-by-step instructions to get this project running locally, run its tests, and understand how
it fits into a CI/CD pipeline.

---

## Prerequisites

| Requirement | Notes |
|---|---|
| **Java 17+ (JDK)** | Required. Verify with `java -version`. Download from [Adoptium](https://adoptium.net/) if missing. |
| **Maven** | **Not required to install separately.** This project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), which downloads the correct Maven version automatically on first run. |
| **Git** | To clone the repo. |
| **An IDE** (optional but recommended) | IntelliJ IDEA, Eclipse, or VS Code with the "Extension Pack for Java" installed (for auto-imports and easier navigation). |

You do **not** need to install Maven manually — always use `./mvnw` (Mac/Linux) or `mvnw.cmd`
(Windows) instead of a bare `mvn` command, unless you have a specific reason to use a separately
installed Maven version.

---

## 1. Clone the repository

```bash
git clone <your-repo-url>
cd <project-folder>
```

---

## 2. Understand the two key config files

| File | Committed to Git? | Purpose |
|---|---|---|
| `pom.xml` | Yes | Declares dependencies, plugins, and build steps for this project. Same for every developer. |
| `settings.xml` (lives at `~/.m2/settings.xml`, **not** in this repo) | No | Machine/user-specific: repository mirrors and credentials (e.g., for a private Nexus/Artifactory repo). Never commit real credentials. |

If your organization uses a private Maven repository (e.g., Nexus), your `settings.xml` needs a
matching `<server>` entry with credentials — ask your team lead for the correct values, or see
your organization's internal onboarding docs. This project itself only depends on public
Maven Central artifacts, so no special repository setup is required to build it.

---

## 3. Build the project

From the project root:

**Mac/Linux:**
```bash
./mvnw clean install
```

**Windows:**
```bash
mvnw.cmd clean install
```

This will:
1. Delete any old build output (`target/`)
2. Compile the source code
3. Compile and run all tests
4. Package the app into an executable `.jar` in `target/`

If you just want to build without installing to your local repo (typical for most local dev
work), use `clean package` instead — see the [command reference](#command-reference) below.

---

## 4. Run the application

**Option A — via Maven (recommended for development):**
```bash
./mvnw spring-boot:run
```

**Option B — run the built jar directly:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```
(Adjust the filename/version to match what's in your `pom.xml` and what was produced in `target/`.)

Either way, the app starts on **http://localhost:8080** by default (embedded Tomcat server — no
separate app server installation needed).

### Try it out
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'

curl http://localhost:8080/api/users
```

---

## Running Tests

Run the full test suite:
```bash
./mvnw test
```

Run a single test class:
```bash
./mvnw test -Dtest=UserServiceTest
```

Run a single test method:
```bash
./mvnw test -Dtest=UserServiceTest#getUserById_returnsUser_whenFound
```

Test result reports (pass/fail details) are written to:
```
target/surefire-reports/
```

Skip tests during a build (not generally recommended, but useful for quick local iteration):
```bash
./mvnw install -DskipTests
```

---

## Command Reference

| Command | What it does |
|---|---|
| `./mvnw clean` | Deletes `target/` (old build output) |
| `./mvnw test` | Compiles and runs all tests |
| `./mvnw clean verify` | Compile + test + package — the standard CI build/test command |
| `./mvnw clean package` | Same as above, produces the runnable `.jar` in `target/` |
| `./mvnw clean install` | Same as `package`, plus copies the jar into your local `~/.m2` repo (mainly useful for local multi-module projects) |
| `./mvnw deploy` | Publishes the jar to a remote repository (Nexus/Artifactory) — used in CD, requires `<distributionManagement>` config and credentials |
| `./mvnw spring-boot:run` | Runs the app directly, without needing to build a jar first |
| `./mvnw dependency:tree` | Prints all resolved dependencies — useful for debugging version/compatibility issues |

---

## Troubleshooting

**"Rename-Item: Access to the path ... is denied" (Windows, first run of `mvnw.cmd`)**
The wrapper is downloading Maven itself and hit a file lock, usually from antivirus/Windows
Defender scanning the temp folder mid-extraction.
- Try running your terminal **as Administrator**, or
- Add `%TEMP%` and `%USERPROFILE%\.m2` to your antivirus exclusions, then retry.

**Compilation errors mentioning `com.fasterxml.jackson.databind` or `MockBean` not found**
This project uses Spring Boot 4 / Jackson 3, where some packages moved:
- `ObjectMapper` → import from `tools.jackson.databind.ObjectMapper` (not `com.fasterxml...`)
- `@MockBean` → replaced by `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`)
- `@WebMvcTest` → now in `org.springframework.boot.webmvc.test.autoconfigure`, provided by the
  `spring-boot-webmvc-test` dependency (already included in this project's `pom.xml`)

**VS Code shows red underlines on imports that actually compile fine via `mvnw`**
VS Code's Java language server cache can go stale after `pom.xml` changes. Fix:
`Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → reload window.

**Can't find `.m2` folder on Windows**
It's a hidden folder at `C:\Users\<you>\.m2`. Type that path directly into File Explorer's
address bar, or enable "Hidden items" under the View tab. To confirm the exact path Maven is
using:
```bash
./mvnw help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

---

## CI/CD Notes

This project is designed to run in CI without any manual Maven installation — the Maven Wrapper
(`mvnw`) downloads the pinned Maven version automatically, so CI runners only need a JDK.

**Typical pipeline stages:**
```
1. Checkout code
2. Set up JDK (e.g., actions/setup-java, or your CI's equivalent)
3. (Optional) Restore ~/.m2 cache — speeds up builds, not required for correctness
4. ./mvnw clean verify        → compiles, runs tests, produces target/*.jar
5. ./mvnw deploy               → publishes the jar to Nexus/Artifactory (CD)
   — or —
   docker build ...            → copies target/*.jar into an image, pushes to a registry
6. (Optional) Save ~/.m2 cache for the next run
```

**Key distinction:**
- `target/` — build output for this one project; wiped by `clean`.
- `~/.m2/repository` — local dependency cache; on most CI runners this is ephemeral and should
  only be treated as a speed optimization, not a deployment target.
- Nexus/Artifactory (via `mvn deploy`) or a container registry (via Docker) — the actual durable,
  shared location other environments/teams should pull the artifact from.

Example GitHub Actions step:
```yaml
- uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '17'

- name: Build and test
  run: ./mvnw clean verify
```

---

## Getting Help

If you hit an error not covered above:
1. Run `./mvnw dependency:tree` and confirm the dependency you'd expect is actually present.
2. Check the exact Spring Boot version in `pom.xml` (`<parent><version>`) — package locations for
   test annotations and Jackson have changed across major versions (3.x → 4.x).
3. Open an issue in this repository with the full error output.
