# Blood Donation

Find a blood donation centre, and check **before you leave home** whether you
are prepared and likely to be accepted, so you don't make a trip only to be
turned away for something you could have known in advance.

An ITE4120 course project, built on the course's e-government-shaped template
([igorboss/ite4120](https://github.com/igorboss/ite4120)): Spring Boot,
PostgreSQL with Liquibase, React with the Helex component library, and a
registry lookup that is imitated locally and X-Road-shaped for production.

## What it does

**Donation centre directory** (`/centres`)
- Lists donation centres with name, region, address, opening hours and phone.
  Search by name or address; filter by region (the 15 Estonian counties).
- Add, edit and retire centres. A centre code identifies exactly one active
  centre; a retired centre's code can be reused (soft delete).
- Each centre's operating organisation is stored only as its business registry
  code. Its legal name and address are fetched live from the business register,
  imitated locally at `/mock-registry/companies/{code}`. If the register is
  down, only that detail is left out; the directory keeps working.

**Readiness questionnaire** (`/readiness`)
- Short yes/no questions about preparation (eaten, drunk, rested…) and
  eligibility today (recent illness, a recent tattoo or piercing…).
- Ends with one of three outcomes: **Ready**, **Prepare first** or
  **Not today**, with advice for every answer that raised a concern.
- **Private by design:** the answers are evaluated in your browser and are
  never sent to the server, stored or logged. There is no answers table and no
  endpoint that accepts answers.

The questionnaire is guidance only. The staff at the donation centre always
make the final decision.

## Specification

Spec first, tests second, implementation third (see `AGENTS.md`):

- User story: [`docs/src/user-stories/BLOOD-US-001.md`](docs/src/user-stories/BLOOD-US-001.md)
- Specification: [`docs/src/specifications/BLOOD.01-donation-readiness.md`](docs/src/specifications/BLOOD.01-donation-readiness.md)
  covers the data model, API, UI and business tests.

## Where the code is

| Layer | Where |
| --- | --- |
| Database: PostgreSQL + Liquibase, schema `blood` | `backend/src/main/resources/blood/db/changelog/` |
| API: Spring Boot + springdoc | `backend/src/main/java/ee/taltech/ite4120/blood/` |
| Business register adapter (mock, HTTP, X-Road skeleton) | `.../blood/operatorregistry/` |
| UI: React + @helex/ui | `frontend/src/pages/Centre*.tsx`, `frontend/src/pages/readiness/` |
| Readiness rule (pure function) | `frontend/src/pages/readiness/evaluate.ts` |

The template's worked example, the **animals register**, is still included
(`/animals`, package `ee.taltech.ite4120.animals`) as the pattern this
component follows.

## Run it

First time on this machine? Install the tools and set up the GitHub token
first ([Tools](#tools), below). Then:

```bash
# 1. database + backend (the first run downloads dependencies, which takes a few minutes)
./scripts/run-backend.sh          # Windows: .\scripts\run-backend.ps1

# 2. frontend, in a second terminal
cd frontend && npm ci && cd ..
./scripts/run-frontend.sh         # Windows: .\scripts\run-frontend.ps1
```

| What | Where |
| --- | --- |
| Donation centres | http://localhost:18640/centres |
| Readiness questionnaire | http://localhost:18640/readiness |
| Swagger UI, the live API contract | http://localhost:18440/swagger-ui.html |
| Imitated business register | http://localhost:18440/mock-registry/companies/90000001 |
| PostgreSQL | localhost:18520 (`scripts/psql` for a shell) |
| Documentation book | `scripts/run-docs` → http://localhost:18740 (needs mdBook) |

You are signed in automatically as a mock user. The API accepts
`Authorization: Bearer <any-username>` in local mode.

**Demo data** (loaded locally under the Liquibase `demo` context): three
fictional centres in Tallinn, Tartu and Pärnu. Operator codes `90000001` and
`90000002` are known to the imitated register; `90000099` shows how an
unknown organisation looks.

The database is disposable: `scripts/reset-db` destroys and recreates it, and
Liquibase rebuilds the schema on the next backend start.

## Tests and checks

Each line runs from the project folder:

```bash
(cd backend && ./gradlew test)      # business tests, against a throwaway PostgreSQL
(cd frontend && npx vitest run)     # the readiness outcome rule
(cd frontend && npx tsc -b)         # typecheck
python3 scripts/check_changesets.py # Liquibase formatted-SQL rules
```

`BloodBusinessRulesIT` holds business tests 1–5 from the specification:
- centre codes are unique among active centres
- coordinates are real and given together
- only active questions are served, in order
- regions must be known
- operator codes have 8 digits

`evaluate.test.ts` holds test 6, the outcome rule. The backend tests also
prove that the migrations run clean from empty.

The same checks run on every pull request as the **Verify** workflow
(`.github/workflows/verify.yml`).

## License

Apache License 2.0, inherited from the course template. See [`LICENSE`](LICENSE).

## Tools

What the scripts and builds in this repository actually call, and why:

| Tool | Version | Used for |
| --- | --- | --- |
| **Git** | any recent | clone, branch, PR — your contribution is the history under your name (gate B) |
| **Docker Desktop** (or a compatible engine with `docker compose`) | running | PostgreSQL locally (`scripts/run-backend`, `reset-db`, `psql`); a throwaway PostgreSQL in the tests (Testcontainers) |
| **JDK 25** | 25 | the Gradle wrapper runs on it *and* compiles with it — nothing downloads a JDK for you (no toolchain resolver is configured), so it must be installed |
| **Gradle** | 9.4 | provided by the wrapper: `backend/gradlew` downloads the pinned version on first use. Nothing to install; a second, system-wide Gradle only invites version mix-ups |
| **Node.js** + npm | 20+ | the Vite frontend (`scripts/run-frontend`, `npm ci`, `npx tsc -b`) |
| **Python 3** | 3.x | `scripts/check-changesets` — the formatted-SQL grammar gate (rule 6 in `AGENTS.md`) |
| **GitHub account** + token with `read:packages` | — | the Helex packages live in GitHub Packages, which refuses anonymous reads |
| GitHub CLI `gh` | optional | pull requests from the terminal; not needed for the token |
| mdBook | optional | serves `docs/` as a book — `scripts/run-docs` |

No local `psql` needed — `scripts/psql` runs it inside the container.

Docker engines other than Docker Desktop (Colima, Rancher Desktop, Podman) work
for running the app, but the tests need to be told where the socket is — with
Colima: `export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock` before
`./gradlew test`.

### Install with a package manager

One script per platform, idempotent, safe to re-run. Each installs the
package manager itself if it is missing, then the rows above, then prints a
version check:

```bash
# macOS — Homebrew
./scripts/setup-tools.sh
```

```powershell
# Windows — Chocolatey, in PowerShell run as Administrator
.\scripts\setup-tools.ps1
```

By hand, the same thing:

| | macOS (Homebrew) | Windows (Chocolatey) |
| --- | --- | --- |
| Git, GitHub CLI | `brew install git gh` | `choco install -y git gh` |
| JDK 25 | `brew install openjdk@25` — keg-only: register it with `sudo ln -sfn "$(brew --prefix openjdk@25)/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-25.jdk` | `choco install -y temurin25` (sets `JAVA_HOME` and `PATH`) |
| Node.js + npm | `brew install node` | `choco install -y nodejs-lts` |
| Python 3 | `brew install python` | `choco install -y python` |
| Docker Desktop | `brew install --cask docker-desktop` | `choco install -y docker-desktop` — needs WSL 2: `wsl --install` once, reboot |
| mdBook (optional) | `brew install mdbook` | no Chocolatey package — see the [mdBook installation guide](https://rust-lang.github.io/mdBook/guide/installation.html) |

Windows notes: run the repository's PowerShell scripts as `.\scripts\name.ps1`;
if PowerShell refuses to run scripts, `Set-ExecutionPolicy -Scope CurrentUser
RemoteSigned` once. Docker Desktop must be started once by hand after install
(it finishes the WSL 2 setup on first start). winget and Scoop carry the same
packages if you prefer them.

### GitHub token

The one step nothing works without: the Helex packages live in GitHub
Packages, which refuses anonymous reads.

**Create the token** at
**[github.com/settings/tokens/new — pre-filled for this project](https://github.com/settings/tokens/new?description=ITE4120+Helex+packages+read-only&scopes=read:packages)**
— the form opens with exactly the one scope this project needs; pick an
expiration, press *Generate token*, copy it once. From a terminal:

```bash
open "https://github.com/settings/tokens/new?description=ITE4120+Helex+packages+read-only&scopes=read:packages"
```

(Windows: paste the URL into the browser, or `start ""` instead of `open`.)
It must be a **classic** token: GitHub's Maven and npm registries do not
accept fine-grained tokens. `read:packages` alone is the minimum privilege —
it can download packages your account can see, and nothing else.

**Both package managers need it.** Gradle reads it from
`~/.gradle/gradle.properties`; npm reads it from its global config — and
GitHub's npm registry refuses anonymous reads **even for public packages**,
so `npm install` in `frontend/` fails with a 401 until this has run once:

```bash
npm config set //npm.pkg.github.com/:_authToken=ghp_YOUR_TOKEN --global
```

**Wire it in** — one paste, silent prompt, no token in your shell history
(macOS/Linux; on Windows edit the two files by hand as shown below):

```bash
read -s TOKEN && printf 'gpr.user=%s\ngpr.key=%s\n' "YOUR_GITHUB_USERNAME" "$TOKEN" >> ~/.gradle/gradle.properties && npm config set "//npm.pkg.github.com/:_authToken=$TOKEN" --global && unset TOKEN
```

What that writes, if you prefer to do it by hand:

```properties
# ~/.gradle/gradle.properties  (backend)
#   Windows: %USERPROFILE%\.gradle\gradle.properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=ghp_YOUR_TOKEN
```

```bash
# npm (frontend) — one command, same on Windows:
npm config set //npm.pkg.github.com/:_authToken=ghp_YOUR_TOKEN --global
```

Missing or wrong token looks like this: Gradle says *cannot resolve
org.helex.emr:…*, npm says *401*. Fix the credentials first — never vendor
jars. No other access is needed: the backend's Helex jars resolve from the
course repository's Maven registry, `igorboss/ite4120` (mirrored by the
lecturer with `scripts/mirror-packages.sh`), and the `@helex-solutions` npm packages are
public — GitHub's npm registry still wants the token to read them, but no
extra permission. A correct token that still gets 401/403 means the token is
expired or your account has no access to `igorboss/ite4120`; ask the
lecturer.
