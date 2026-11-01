package com.example

import com.example.ai.ExplanationService
import com.example.data.models.Evidence
import com.example.data.models.Explanation
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import com.example.data.models.Severity
import com.example.data.models.SuggestedAction
import com.example.export.ReportGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CostGuardianFinOpsTest {
    private fun liveDiskFinding(): Finding {
        val finding = Finding(
            id = "disk-1",
            cloud = "gcp",
            checkId = "gcp.unattached_disk",
            type = "unattached_disk",
            resourceName = "orphan-disk",
            resourceUrl = "https://console.cloud.google.com/compute/disksDetail/zones/us-central1-a/disks/orphan-disk?project=guardian-project",
            project = "guardian-project",
            location = "us-central1-a",
            severity = Severity.MEDIUM,
            evidence = Evidence(
                metrics = mapOf("size_gb" to "100", "disk_type" to "pd-standard", "status" to "READY", "users" to "0"),
                notes = listOf("Live inventory reports no attached instances.")
            ),
            action = SuggestedAction(
                kind = "review_disk",
                command = "gcloud compute disks describe orphan-disk --zone=us-central1-a --project=guardian-project",
                reversible = true,
                risk = "Check ownership and data retention before taking action."
            )
        )
        return finding.copy(explanation = ExplanationService.generateDeterministicExplanation(finding))
    }

    @Test
    fun deterministicGuidanceDoesNotPretendInventoryProvesCostOrOwnership() {
        val finding = liveDiskFinding()
        val explanation = requireNotNull(finding.explanation)
        assertTrue(explanation.whyItMatters.contains("billed cost"))
        assertTrue(explanation.recommendedFix.contains("owning team"))
        assertTrue(explanation.riskNote.contains("needed data"))
    }

    @Test
    fun markdownReportContainsOnlyObservedInventoryAndReviewCommands() {
        val finding = liveDiskFinding()
        val summary = ScanSummary(
            scanId = "scan-1",
            projectName = finding.project,
            status = "live_gcp_read_only",
            startedAt = 1_730_000_000_000L,
            findingsCount = 1
        )
        val report = ReportGenerator.generateMarkdownReport(summary, listOf(finding))
        assertTrue(report.contains("live, read-only Compute Engine inventory", ignoreCase = true))
        assertTrue(report.contains("Billing data was not requested"))
        assertTrue(report.contains("size gb: 100"))
        assertTrue(report.contains("gcloud compute disks describe"))
        assertFalse(report.contains("Estimated Monthly Opportunity"))
        assertFalse(report.contains("\$42.50"))
        assertFalse(report.contains("AWS"))
    }
}
