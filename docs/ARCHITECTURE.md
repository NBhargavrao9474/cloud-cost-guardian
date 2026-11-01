# Architecture

## Data flow

```text
User
  │ approves Google sign-in and enters a project ID
  ▼
Compose UI ── ViewModel ── Repository
                              │
                              ├── Google Identity Services token
                              │          │ compute.readonly
                              │          ▼
                              │     Compute Engine aggregated APIs
                              │       disks / addresses / instances
                              │          │ paginated inventory
                              │          ▼
                              ├── deterministic inventory rules
                              │          │ observed evidence only
                              │          ▼
                              ├── Room local scan history
                              └── report generator / optional explanation
```

## Main components

| Area | Source | Responsibility |
| --- | --- | --- |
| App entry | `app/src/main/java/com/example/MainActivity.kt` | Initializes the Compose application and screen. |
| Dashboard | `app/src/main/java/com/example/ui/` | Presents connection state, findings, history, and user actions. |
| Scanner | `app/src/main/java/com/example/engine/GcpComputeScanner.kt` | Calls read-only Compute Engine APIs, follows pagination, and derives inventory findings. |
| Repository | `app/src/main/java/com/example/data/repository/GuardianRepository.kt` | Coordinates scans and local persistence. |
| Database | `app/src/main/java/com/example/data/db/` | Stores scan and finding history on device. |
| Reports | `app/src/main/java/com/example/export/ReportGenerator.kt` | Formats a shareable inventory report. |
| Explanations | `app/src/main/java/com/example/ai/ExplanationService.kt` | Provides offline deterministic guidance and an optional, separately configured Firebase AI Logic explanation. |

## Finding rules

The scanner reports only observed inventory conditions:

- A disk is in `READY` state and has no attached users.
- A reserved external IP address has no assigned instance.
- An instance is terminated and still has persistent disks attached.

These rules identify candidates for review. They do not prove that the resource is unused, calculate its price, or recommend automatic deletion.

## Security boundaries

- OAuth requests Compute Engine read-only access; no write scope is requested.
- The access token is kept in memory for the scan.
- Read-only `gcloud` describe commands are displayed for the user to run if desired; the app never executes them.
- Hosted Gemini is optional and unconfigured in the submitted build. If enabled by an owner, it receives selected resource details and observed inventory evidence, not billing data.
