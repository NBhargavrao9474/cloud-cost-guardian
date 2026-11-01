# Cloud Cost Guardian

**A read-only Android reviewer for Google Cloud Compute Engine inventory.** Cloud Cost Guardian helps teams find resources that may need a human review, explains the evidence it observed, and never changes cloud resources.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Minimum SDK](https://img.shields.io/badge/Min%20SDK-24-blue)](https://developer.android.com/about/versions/nougat)

## Try the app

**[Download the Android demo APK](https://github.com/NBhargavrao9474/cloud-cost-guardian/releases/latest/download/CloudCostGuardian-Demo.apk)** · [View all releases](https://github.com/NBhargavrao9474/cloud-cost-guardian/releases)

The APK is a debug-signed hackathon build for direct Android installation. It was installed and launched on a physical Android 16 phone and an Android 16 emulator. Android will ask you to approve installing an app downloaded from your browser or file manager.

> **Live-scan requirement:** this repository has no connected Google Cloud project or OAuth client. Live mode starts empty and shows only resources returned by a real scan. For a presentation without a cloud project, tap **Try illustrative demo data**; the app labels those fictional examples and keeps them out of live scan history.

## Hackathon overview

### The problem

Cloud resources can be left behind after experiments and deployments. Teams need a quick way to identify resources that deserve attention, while avoiding risky automatic deletion based on incomplete information.

### The solution

Cloud Cost Guardian uses read-only Compute Engine inventory to surface a small set of review signals. Each finding includes the observed resource evidence and a read-only inspection command. A person remains responsible for checking ownership and dependencies before taking any action.

### Current features

1. **Read-only Google Cloud connection.** The user enters a project ID, signs in with Google, and approves the Compute Engine `compute.readonly` scope. OAuth access tokens are held in memory during the scan.
2. **Live inventory collection.** The app lists disks, reserved external IP addresses, and VM instances with paginated Compute Engine aggregate APIs.
3. **Transparent review findings.** It flags an unattached `READY` disk with a non-zero size, an unassigned reserved external IP address, or a terminated VM that still has persistent disks attached. Each finding shows the observed inventory evidence and why a human should review it.
4. **Safer next steps.** Findings include a copyable read-only `gcloud` describe command. The app does not execute commands or make changes to cloud resources.
5. **Useful follow-up tools.** Search and severity filters help focus the list; scan history is stored locally; charts summarize returned findings; a report can be shared.
6. **Offline guidance.** Deterministic guidance works without an AI service. A user may request an optional hosted Gemini explanation after the owner configures Firebase AI Logic. That hosted integration is not configured or verified in this submission.

### Illustrative example: what a scan could show

The following fictional inventory is a documentation example only. **It is not stored in the APK, does not appear in the app, and was not scanned from Google Cloud.** It shows how the current rules would interpret matching live inventory:

| Fictional resource | Illustrative inventory evidence | Possible app finding | What the user should check |
| --- | --- | --- | --- |
| `temp-build-cache-01` disk in `us-central1-a` | `READY`, 100 GiB, no attached instances | Unattached disk (medium severity) | Check owner, snapshots, retention needs, and dependencies. |
| `demo-reserved-ip` address | External, `RESERVED`, no assigned instance; example address `203.0.113.27` | Unassigned reserved external IP (low severity) | Confirm it is not reserved for a planned service before releasing it. |
| `retired-api-test` VM in `us-central1-a` | `TERMINATED`, one attached persistent disk | Terminated VM retaining a persistent disk (low severity) | Review the VM and disk ownership and lifecycle. |

The app would show evidence and a read-only inspection command for a finding. It would **not** calculate a dollar amount or tell the user that deletion is safe. The example values above are not valid proof of a real finding. See [the demo walkthrough](docs/DEMO_WALKTHROUGH.md) for a presenter-ready script.

### How it helps

- **Finds review candidates faster:** brings a few easy-to-miss inventory conditions into one view.
- **Makes the signal explainable:** shows the resource status and attachment evidence behind each flag.
- **Reduces risky guesswork:** keeps access read-only and leaves ownership checks and any cleanup decision to a person.
- **Helps teams communicate:** local history and shareable reports make scan results easier to review together.

### What it does not claim

The app does not read Cloud Billing data, Cloud Monitoring metrics, or Recommender results. It does not calculate actual cost or savings, and a flagged resource is not proof that the resource is unnecessary. It does not delete or modify cloud resources. The optional hosted AI integration is not configured; the tested guidance is deterministic and on-device. This project is **not a Gemma 4 implementation**.

## Demo flow

1. Open the app and show that no findings are invented before a scan.
2. If no GCP sandbox is available, tap **Try illustrative demo data** and point out the persistent demo-only banner.
3. Open findings, use search and severity filters, view the summary charts, and show the detail/evidence panel.
4. Open sharing/export and point out that the generated report is labeled illustrative demo data.
5. Exit demo mode to return to the empty live state. Explain that real scans require a tester-provided GCP project and explicit read-only access.

See [HACKATHON_SUBMISSION.md](HACKATHON_SUBMISSION.md) for a copy-ready project pitch and [the demo walkthrough](docs/DEMO_WALKTHROUGH.md) for an explicitly fictional example and presenter talk track.

## Architecture

```text
Jetpack Compose UI
       │
       ├── Google Identity Services (user-approved OAuth)
       │       └── Compute Engine read-only APIs
       │
       ├── Inventory scanner ── deterministic finding rules
       │
       ├── Room database ── local scan history
       │
       └── Report generator ── shareable inventory summary
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for component responsibilities and data flow.

## Build from source

Requirements: Android Studio, JDK 17 or later, Android SDK Platform 37.2, and Android Build Tools 36.0.0 or later.

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The AI Studio generated build uses a project-local debug keystore. It is not included in this repository. If you are building from a clean checkout and the key is missing, follow the one-time local-key step in [README build notes](README.md#debug-signing-key-for-a-clean-checkout).

### Debug signing key for a clean checkout

Create a local-only key before the first build:

```powershell
keytool -genkeypair -v -keystore .\debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

This key is only for local debug builds. Do not use it for a release or upload it publicly. A newly generated key cannot update an APK signed with a different key; uninstall the older build first if Android reports a signature conflict.

Run local checks with:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

See [docs/TESTING.md](docs/TESTING.md) for the recorded verification and its limits.

## Configure a live Google Cloud scan

A tester must supply and configure a Google Cloud project. This repository contains no cloud credentials or OAuth client.

1. Enable the Compute Engine API in the tester's project.
2. Grant the signed-in Google account the **Compute Viewer** role.
3. Configure the OAuth consent screen and add the account as a test user if required.
4. Create an Android OAuth client for package `com.aistudio.costguardian.vknmrx`, using the SHA-1 certificate fingerprint of the signing key.
5. Install the app, choose **Connect Google Cloud · Read-only**, enter the project ID, and approve the requested read-only access.

The access token is held in memory for the scan. Use **Disconnect Google Cloud access** to revoke authorization. See [docs/GOOGLE_CLOUD_SETUP.md](docs/GOOGLE_CLOUD_SETUP.md) for details and troubleshooting.

## Privacy and safety

- Requests only the Compute Engine read-only OAuth scope.
- Keeps Google access tokens in memory rather than storing them on disk.
- Stores scan history locally; Android cloud backup excludes the app database and preferences.
- Displays commands for human review; does not run them or make cloud changes.
- Does not transmit billing values because it does not collect them.

## Project structure

```text
app/src/main/java/com/example/
├── ai/             Optional Firebase AI Logic explanation service
├── data/           Repository, data models, and Room database
├── engine/         Google Cloud Compute Engine inventory scanner
├── export/         Shareable report generation
└── ui/             Compose screens, view model, and UI components

docs/               Architecture, setup, testing, and hackathon notes
gradle/             Gradle wrapper and dependency catalog
```

## License

No reuse license is included yet. Contact the author before redistributing or incorporating this code into another project.
