package com.example.ui

import com.example.data.models.Evidence
import com.example.data.models.Explanation
import com.example.data.models.Finding
import com.example.data.models.Severity
import com.example.data.models.SuggestedAction

/** Synthetic, in-memory examples for presentations. Never persisted as a real scan. */
object DemoFindings {
    private const val PROJECT = "illustrative-demo-only"

    val items = listOf(
        Finding(
            id = "demo-disk-001", cloud = "GCP", checkId = "unattached_disk",
            type = "unattached_disk", resourceName = "temp-build-cache-01",
            project = PROJECT, location = "us-central1-a", severity = Severity.MEDIUM,
            evidence = Evidence(
                metrics = mapOf("size_gb" to "100", "disk_type" to "pd-ssd", "status" to "READY", "users" to "0"),
                notes = listOf("Illustrative demo data only; not returned by Google Cloud.", "No attached instances are shown in this fictional example.", "Billing data is not connected; no cost or savings amount is available.")
            ),
            action = SuggestedAction("demo_only", "DEMO ONLY - no command is run", true, "Check ownership, snapshots, retention, and dependencies before any real action."),
            explanation = Explanation("Example: ready disk has no attached instances.", "Persistent storage can remain after a temporary workload ends.", "Verify the owner, snapshots, and retention needs in the real project.", "This synthetic example is not evidence about a real cloud resource.", "template")
        ),
        Finding(
            id = "demo-ip-001", cloud = "GCP", checkId = "unused_ip",
            type = "unused_ip", resourceName = "demo-reserved-ip (203.0.113.27)",
            project = PROJECT, location = "global", severity = Severity.LOW,
            evidence = Evidence(
                metrics = mapOf("address" to "203.0.113.27", "status" to "RESERVED", "address_type" to "EXTERNAL", "attached_to" to "None"),
                notes = listOf("Illustrative demo data only; 203.0.113.27 is a documentation example address.", "Confirm an address is not held for a planned service before releasing it.")
            ),
            action = SuggestedAction("demo_only", "DEMO ONLY - no command is run", true, "Verify planned use and ownership before releasing a real address."),
            explanation = Explanation("Example: reserved external address is unassigned.", "A reserved address may be intentionally held for a future service.", "Check the service owner and deployment plan before taking action.", "This synthetic example is not evidence about a real cloud resource.", "template")
        ),
        Finding(
            id = "demo-vm-001", cloud = "GCP", checkId = "stopped_vm",
            type = "stopped_vm", resourceName = "retired-api-test",
            project = PROJECT, location = "us-central1-a", severity = Severity.LOW,
            evidence = Evidence(
                metrics = mapOf("status" to "TERMINATED", "attached_persistent_disks" to "1"),
                notes = listOf("Illustrative demo data only; not returned by Google Cloud.", "The fictional terminated VM retains one persistent disk.", "Disk charges have not been measured.")
            ),
            action = SuggestedAction("demo_only", "DEMO ONLY - no command is run", true, "Review VM and disk ownership and lifecycle; never assume deletion is safe."),
            explanation = Explanation("Example: terminated VM still has a persistent disk.", "The VM is stopped, but its disk remains a separate resource.", "Identify the disk owner and retention needs before any cleanup.", "This synthetic example is not evidence about a real cloud resource.", "template")
        )
    )
}
