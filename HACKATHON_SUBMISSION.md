# Hackathon submission brief: Cloud Cost Guardian

## Project overview

Cloud Cost Guardian is an Android app that helps Google Cloud users review Compute Engine inventory for resources that may deserve attention. It uses read-only access and shows the evidence behind each finding, with safe review steps users can choose to take themselves.

## Problem

Cloud resources can be left behind after experiments and deployments. Finding them takes time, and deleting a resource without checking its purpose can disrupt a workload.

## Solution

Cloud Cost Guardian reviews Compute Engine disks, external IP addresses, and VM instances for a small set of inventory conditions. It presents observed evidence and read-only inspection commands. It does not change resources or claim that any finding is definitely waste.

## Current capabilities

- Scans disks, reserved external IP addresses, and instances with read-only access.
- Flags unattached ready disks, unassigned reserved external IPs, and terminated VMs that still have persistent disks.
- Saves scan history locally and shares an inventory report.
- Provides deterministic offline guidance; optional hosted Gemini explanations require separate Firebase configuration.

## Demo status

The APK was tested on a physical phone and emulator; see [the verification record](docs/TESTING.md). A live GCP scan is not demonstrated because this submission has no connected cloud project or OAuth client. The app does not access billing data or compute actual costs and savings. Do not claim a tested Gemma 4 feature; this prototype is not a Gemma 4 implementation.

## Short pitch

> Cloud Cost Guardian helps teams review Google Compute Engine inventory for resources that may have been left behind. It uses read-only access, explains each flag with the evidence it observed, and leaves every cloud action to the user. The prototype focuses on safer, more transparent resource review; billing-based savings estimates are outside its current scope.

## Demo talk track

1. Open the app and point out that an empty account shows no invented findings.
2. Explain that connecting requests read-only Compute Engine access and a project ID.
3. Walk through the inventory checks and how findings show observed evidence and a read-only inspection command.
4. Show scan history and report sharing.
5. State that a live scan requires a judge-provided GCP sandbox; do not present a fabricated scan as real.

## Track fit

This is an Android and Google Cloud inventory prototype. Confirm that the event accepts this scope before submitting to a model-specific or live-cloud track.
