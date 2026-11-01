# Cloud Cost Guardian — As-Built Product Plan

## Product purpose

Cloud Cost Guardian is an Android app that reviews live Google Cloud Compute Engine inventory with user-approved, read-only access. It helps a human identify resources worth checking. Inventory signals do not prove waste, ownership, or that a resource is safe to remove.

## Implemented scope

- Google authorization requests the `compute.readonly` OAuth scope.
- The scanner lists aggregated Compute Engine disks, external IP addresses, and instances, including pagination.
- It flags READY unattached disks, unassigned reserved external IP addresses, and terminated instances that still have persistent disks attached.
- Findings show observed inventory evidence, review risks, and copyable read-only `gcloud` describe commands. The app never executes commands or modifies cloud resources.
- Users can filter/search findings, review locally saved live scans, export a Markdown inventory report, and share an inventory summary.
- Deterministic guidance works offline. Hosted Gemini explanations are optional and require the owner's Firebase AI Logic and App Check configuration.
- Current database migrations remove the previous prototype's simulated scan records and pricing/grade columns.

## Explicit limits

The app does not access Cloud Billing, billing exports, Cloud Monitoring metrics, Recommender, AWS, or other providers. It does not estimate or report costs or savings. It contains no demo scan, seeded cloud resources, simulated PIN/biometric gate, or automatic remediation.

## Privacy and permissions

Google access is requested from the user at scan time. The access token stays in memory and is not persisted. Inventory history remains on the device; the database and shared preferences are excluded from Android backup and device transfer. If the user requests hosted Gemini, the selected finding's resource name, type, location, and observed inventory evidence are sent to Firebase AI Logic. Billing amounts are not collected or sent.

## Owner configuration required

For live scans, enable the Compute Engine API, grant the user Compute Viewer access, configure the OAuth consent screen and Android OAuth client for the app ID/signing certificate, then approve the scope on-device. This workspace contains no GCP OAuth client or cloud credentials. Hosted Gemini additionally requires Firebase project configuration. A distributable release APK also requires an owner-controlled release keystore.

## Latest local verification

- 6 unit tests passed.
- 4 Android tests passed on both the CPH2757 phone and API 36 emulator, including the empty state, read-only connection flow, project ID validation, and old database migration.
- Debug APK installed and launched on both devices without a fatal application exception.
- Debug build, release Kotlin compilation, and lint passed; lint has one API 36 target warning.
- `assembleRelease` is not available until release signing credentials are provided.
