# Verification record

## Automated checks

| Check | Result |
| --- | --- |
| `testDebugUnitTest` | Passed: 6 unit tests. |
| `connectedDebugAndroidTest` | Passed: 4 instrumented tests on each device. |
| `assembleDebug` | Passed. |
| `compileReleaseKotlin` | Passed. |
| `lintDebug` | Passed with 0 errors and 1 `OldTargetApi` warning. |

## Devices and flows

The debug APK was installed and launched on a physical Android 16 phone and an Android 16 emulator (API 36). Instrumented coverage includes the empty state (no invented findings), the read-only GCP connection dialog and project ID validation, and a Room database migration check.

The opt-in illustrative demo mode is stored only in the current UI session and bypasses scan persistence. The demo dataset includes three synthetic examples; the report/export path labels them as illustrative rather than live inventory.

## Limits of verification

A live cloud scan was not run because no Google Cloud project or OAuth client is configured in the workspace. Firebase AI Logic is also unconfigured; offline deterministic guidance is the explanation path verified with this build. A release APK has not been signed because the project has no owner-controlled release keystore. The submitted APK is for private hackathon demonstration and local installation, not Play Store distribution.
