package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import com.example.export.ReportGenerator
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareableAuditCardDialog(
    projectName: String,
    findings: List<Finding>,
    onDismiss: () -> Unit,
    isDemoMode: Boolean = false
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().testTag("shareable_audit_card_dialog"),
            color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDemoMode) "Illustrative demo review" else "GCP inventory review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }
                Text("$projectName · ${findings.size} resources for review",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(if (isDemoMode) "Fictional demo examples only. No cloud project was scanned; these are not real findings." else "Live, read-only Compute Engine inventory. Billing data is not connected, so this report has no verified cost or savings figures.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { ReportGenerator.shareInventorySummary(context, projectName, findings, isDemoMode) },
                    colors = ButtonDefaults.buttonColors(containerColor = FinOpsEmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("share_summary_button")) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("Share inventory summary", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val summary = ScanSummary(
                        scanId = "inventory-export",
                        projectName = projectName,
                        status = if (isDemoMode) "illustrative_demo" else "live_gcp_read_only",
                        startedAt = System.currentTimeMillis(),
                        findingsCount = findings.size
                    )
                    ReportGenerator.shareReport(context, ReportGenerator.generateMarkdownReport(summary, findings),
                        "GCP inventory review: $projectName", "Share inventory review")
                }, modifier = Modifier.fillMaxWidth().testTag("export_markdown_report_button")) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text("Export full Markdown report", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
