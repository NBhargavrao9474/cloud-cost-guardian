# Cloud Cost Guardian — Build Guide & Working Spec

Updated: Nov 1, 2026

## Product overview

Cloud Cost Guardian is an Android app that reviews live Google Cloud Compute Engine inventory with explicit user authorization and read-only API scope. It helps an engineer find resources to investigate; it does not determine actual waste, ownership, or whether a resource is safe to remove.

## Implemented capabilities

- Android app built with Kotlin and Jetpack Compose.
- Google Identity Services authorization requests `compute.readonly`.
- Paged aggregated inventory reads for disks, external IP addresses, and VM instances.
- Review findings for READY unattached disks, unassigned reserved external IPs, and terminated VMs that retain persistent disks.
- Evidence, risk guidance, and copy-only `gcloud` describe commands. The app never runs these commands or mutates cloud resources.
- Search, severity filters, on-device live-scan history, Markdown reports, and share sheets.
- Deterministic on-device explanations. Optional hosted Gemini explanations use Firebase AI Logic only when the owner configures Firebase and App Check.

## Explicitly out of scope

This app does not connect to Cloud Billing, Billing export, Cloud Monitoring, Recommender, AWS, or Azure. It does not calculate costs/savings, inspect CPU utilization, claim idle or oversized instances, or offer automated remediation. It has no seeded or simulated findings, demo scans, fake PIN/biometric lock, or illustrative cloud providers.

## Read-only safety and data handling

The app requests an OAuth access token for a scan, keeps that token in memory, and does not write it to disk. Scan history remains on-device, and Android backup excludes the local database and shared preferences. Review commands are displayed for a person to inspect and are never executed.

If a user explicitly requests a hosted explanation, the selected resource name, type, location, and observed inventory evidence are sent to Firebase AI Logic. The app does not collect billing data. Guidance is not authorization to change infrastructure.

## Run and verify

Prerequisites: Android Studio or Gradle, JDK 17+, Android SDK Platform 37.2, and Build Tools 36.0.0+.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The current debug APK has been built and installed on the CPH2757 phone and API 36 emulator. Unit and Android tests pass. Lint reports no errors and one target API warning. `assembleRelease` needs the owner's private release keystore and signing credentials.

## Owner setup required for a live scan

Enable the Compute Engine API, grant the Google account Compute Viewer on the target project, configure OAuth consent and an Android OAuth client for application ID `com.aistudio.costguardian.vknmrx` and the signing key's SHA-1, then approve the read-only scope on-device. This project workspace has no OAuth client or GCP credentials, so a live cloud query has not been verified.

For optional hosted Gemini, configure a Firebase project with Firebase AI Logic, the Gemini Developer API provider, `google-services.json`, and App Check. Offline deterministic explanations do not need Firebase.
