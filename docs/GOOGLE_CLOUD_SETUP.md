# Google Cloud setup for a live scan

A live scan is optional for installing and exploring the APK, but is needed to demonstrate real cloud inventory. This repository does not include a Google Cloud project, OAuth client, or credentials. Use a tester-owned or organizer-provided sandbox; do not create billable resources for a demo.

## Prerequisites

- A Google Cloud project with the Compute Engine API enabled.
- The Google account used in the app granted the **Compute Viewer** role on that project.
- An OAuth consent screen configured for the app; add the test account to the audience if the consent configuration is in testing mode.
- An Android OAuth client configured for the app's package and signing certificate.

## Configure Android OAuth

Application ID: `com.aistudio.costguardian.vknmrx`.

For the local project debug key, obtain its SHA-1 fingerprint with:

```powershell
keytool -list -v -keystore .\debug.keystore -alias androiddebugkey -storepass android -keypass android | Select-String SHA1
```

Create an Android OAuth client in the tester's Google Cloud credentials with this package name and SHA-1 certificate fingerprint. Do not commit OAuth credentials or `google-services.json`.

## In the app

1. Install and open the APK.
2. Choose **Connect Google Cloud · Read-only**.
3. Enter the project ID (not its display name or numeric project number).
4. Sign in with the Google account that has Compute Viewer access.
5. Review and approve the requested Compute Engine read-only permission.
6. Run a scan. Review each finding's evidence before deciding whether any resource needs action.

Use **Disconnect Google Cloud access** to revoke the app's Google authorization. The app does not change cloud resources.

## Troubleshooting

- **403 / permission denied:** check that the Compute Engine API is enabled and the account has Compute Viewer on the selected project.
- **OAuth app not configured / app not verified:** check the Android package name, signing SHA-1, consent-screen audience, and test-user list.
- **No findings:** this can be a valid result; the scanner does not add examples or fabricate results.
- **No live project available:** use the APK to present the tested interface and explain the live-scan prerequisite. Do not present synthetic resources as real.
