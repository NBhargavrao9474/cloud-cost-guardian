# Cloud Cost Guardian

**A read-only Android reviewer for Google Cloud Compute Engine inventory.** Cloud Cost Guardian helps teams find resources that may need a human review, explains the evidence it observed, and never changes cloud resources.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Minimum SDK](https://img.shields.io/badge/Min%20SDK-24-blue)](https://developer.android.com/about/versions/nougat)

## Try the app

**[Download the Android demo APK](https://github.com/NBhargavrao9474/cloud-cost-guardian/releases/latest/download/CloudCostGuardian-Demo.apk)** · [View all releases](https://github.com/NBhargavrao9474/cloud-cost-guardian/releases)

The APK is a debug-signed hackathon build for direct Android installation. It was installed and launched on a physical Android 16 phone and an Android 16 emulator. Android will ask you to approve installing an app downloaded from your browser or file manager.

> **Live-scan requirement:** this repository has no connected Google Cloud project or OAuth client. You can install and explore the app, but a real scan requires a Google Cloud project configured by the tester. The app starts empty and does not show fabricated resources.

## Hackathon overview

### The problem

Cloud resources can be left behind after experiments and deployments. Teams need a quick way to identify resources that deserve attention, while avoiding risky automatic deletion based on incomplete information.

### The solution

Cloud Cost Guardian uses read-only Compute Engine inventory to surface a small set of review signals. Each finding includes the observed resource evidence and a read-only inspection command. A person remains responsible for checking ownership and dependencies before taking any action.

### What works today

- Connects to Google Cloud with the `compute.readonly` OAuth scope.
- Lists Compute Engine disks, reserved external IP addresses, and VM instances through paginated aggregate APIs.
- Flags unattached `READY` disks, unassigned reserved external IPs, and terminated VMs that still have persistent disks attached.
- Shows the evidence behind a finding and copyable `gcloud` describe commands; the app never executes the commands.
- Searches and filters findings, saves scan history locally, and shares inventory reports.
- Provides deterministic offline guidance. Optional hosted Gemini explanations need separate Firebase configuration.

### What it does not claim

The app does not read Cloud Billing data, Cloud Monitoring metrics, or Recommender results. It does not calculate actual cost or savings, and a flagged resource is not proof that the resource is unnecessary. It does not delete or modify cloud resources. The optional hosted AI integration is not configured; the tested guidance is deterministic and on-device. This project is **not a Gemma 4 implementation**.

## Demo flow

1. Open the app and show that no findings are invented before a scan.
2. Explain that a live scan requires a tester-provided Google Cloud project and explicit read-only access.
3. Walk through the three inventory checks and the evidence shown for each finding.
4. Show local scan history and the shareable report flow.
5. If no sandbox project is available, explain the live-scan limitation rather than presenting sample data as real.

See [HACKATHON_SUBMISSION.md](HACKATHON_SUBMISSION.md) for a copy-ready project pitch and demo talk track.

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
