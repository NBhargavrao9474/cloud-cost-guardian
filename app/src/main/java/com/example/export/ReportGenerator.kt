package com.example.export

import android.content.Context
import android.content.Intent
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {
    fun generateMarkdownReport(summary: ScanSummary, findings: List<Finding>): String {
        val isDemo = summary.status == "illustrative_demo"
        val scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(summary.startedAt))
        return buildString {
            appendLine(if (isDemo) "# Cloud Cost Guardian — ILLUSTRATIVE DEMO ONLY" else "# Cloud Cost Guardian — GCP Inventory Review")
            appendLine()
            appendLine(if (isDemo) "> Synthetic presentation example only. This is not a live GCP scan and describes no real cloud resources." else "> Live, read-only Compute Engine inventory. No cloud resources were modified. Billing data was not requested, so this report contains no verified costs or savings.")
            appendLine()
            appendLine("**Project:** `${summary.projectName}`  ")
            appendLine("**Scan date:** $scanDate  ")
            appendLine("**Review items:** ${findings.size}  ")
            appendLine()
            appendLine("| Resource | Type | Location | Severity | Observed evidence |")
            appendLine("| :--- | :--- | :--- | :--- | :--- |")
            findings.forEach { finding ->
                val evidence = finding.evidence.metrics.entries.joinToString(", ") { (key, value) -> "${key.replace('_', ' ')}: $value" }
                    .replace("|", "\\|")
                appendLine("| `${finding.resourceName}` | `${finding.type}` | `${finding.location}` | ${finding.severity.label} | $evidence |")
            }
            appendLine()
            findings.forEachIndexed { index, finding ->
                appendLine("## ${index + 1}. ${finding.resourceName}")
                appendLine("- **Finding:** ${finding.explanation?.summary.orEmpty()}")
                appendLine("- **Why review it:** ${finding.explanation?.whyItMatters.orEmpty()}")
                appendLine("- **Recommended review:** ${finding.explanation?.recommendedFix.orEmpty()}")
                appendLine("- **Risk:** ${finding.explanation?.riskNote.orEmpty()}")
                appendLine()
                appendLine("Read-only command example (not executed):")
                appendLine("```bash")
                appendLine(finding.action.command)
                appendLine("```")
                appendLine()
            }
            appendLine("---")
            appendLine(if (isDemo) "*Illustrative synthetic data only. Not generated from live GCP inventory.*" else "*Generated from live GCP Compute Engine inventory. Costs, savings, ownership, and resource intent are not verified by this app.*")
        }
    }

    fun shareReport(context: Context, reportText: String, subject: String, chooserTitle: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, reportText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }

    fun shareInventorySummary(context: Context, projectName: String, findings: List<Finding>, isDemo: Boolean = false) {
        val counts = findings.groupingBy { it.type.replace('_', ' ') }.eachCount()
        val text = buildString {
            appendLine(if (isDemo) "Cloud Cost Guardian · ILLUSTRATIVE DEMO ONLY" else "Cloud Cost Guardian · GCP inventory review")
            appendLine("Project: $projectName")
            appendLine(if (isDemo) "Fictional examples · ${findings.size} review signals · not a live scan" else "Live read-only inventory · ${findings.size} resources for review")
            counts.forEach { (type, count) -> appendLine("• $type: $count") }
            appendLine(if (isDemo) "Synthetic data only. No cloud project was scanned or changed; no cost or savings is represented." else "No resources were modified. Billing data was not accessed; costs and savings are unavailable.")
        }
        shareReport(context, text, "GCP inventory review: $projectName", "Share inventory review")
    }
}
