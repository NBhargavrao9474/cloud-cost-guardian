# Demo walkthrough (illustrative data only)

This is a presenter aid for explaining what the app's current live inventory rules do. **These values are fictional. They are not packaged in the app, are not displayed as scan results, and did not come from a Google Cloud project.** The app remains empty until a user connects a project and scans its real inventory.

## Example inventory and expected review signals

| Resource | Illustrative inventory values | Expected review signal | Human follow-up |
| --- | --- | --- | --- |
| Disk `temp-build-cache-01` | Zone `us-central1-a`; status `READY`; size `100` GiB; attached users: none | Unattached disk, medium severity | Verify owner, snapshots, retention, and dependencies. |
| Address `demo-reserved-ip` | External address `203.0.113.27`; status `RESERVED`; assigned users: none | Unassigned reserved external IP, low severity | Check whether it is held for a planned service before releasing it. |
| VM `retired-api-test` | Zone `us-central1-a`; status `TERMINATED`; attached persistent disks: `1` | Terminated VM retaining a persistent disk, low severity | Identify the disk owner and lifecycle before making a decision. |

`203.0.113.27` is from a documentation-only example range. The resource names and inventory records are illustrative; do not treat this table as a real Google Cloud scan.

## What a real user would see

1. The dashboard begins with an empty state; it does not inject this example into scan history.
2. The user selects **Connect Google Cloud · Read-only**, enters a real project ID, signs in, and approves the Compute Engine read-only request.
3. The app lists real disks, addresses, and instances using the Compute Engine API.
4. The scanner applies its current inventory rules and lists only findings supported by the returned inventory.
5. Opening a finding shows its evidence and a copyable read-only inspection command.
6. The user checks ownership and dependencies. The app does not run the command, change the cloud project, or claim savings.

A live scan requires a tester-provided GCP project and OAuth setup; see [Google Cloud setup](GOOGLE_CLOUD_SETUP.md). If the presenter has no project available, they can explain the example verbally or show this table in the README. They must not label it as an on-device scan.

## Short presentation script

> “This table is a fictional walkthrough, not data inside the app. If a connected project returned a ready unattached disk, an unused reserved external IP, or a stopped VM with a persistent disk, Cloud Cost Guardian would show the evidence and a read-only command to inspect it. It does not estimate costs because billing data is not connected, and it never deletes resources. The user decides what to investigate.”
