package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Finding
import com.example.ui.theme.FinOpsAmberWarning
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsOutline
import com.example.ui.theme.FinOpsOutlineVariant
import com.example.ui.theme.FinOpsRubyCritical

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingDetailSheet(
    finding: Finding?,
    isAiLoading: Boolean,
    onDismiss: () -> Unit,
    onRequestAi: (Finding) -> Unit,
    onCopyCommand: (String) -> Unit
) {
    if (finding == null) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("finding_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = finding.type.replace('_', ' ').uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FinOpsCyanSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = finding.resourceName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${finding.cloud.uppercase()} • Location: ${finding.location} • Project: ${finding.project}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Billing status is explicit: this app reads inventory but does not read bills.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B12))
                    .border(1.dp, FinOpsCyanSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("BILLING DATA", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Not connected", style = MaterialTheme.typography.titleLarge,
                        color = FinOpsCyanSecondary, fontWeight = FontWeight.Bold)
                    Text("Costs and savings cannot be verified from inventory alone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Evidence & Telemetry Grid
            Text(
                text = "OBSERVED TELEMETRY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                finding.evidence.metrics.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key.replace('_', ' '),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Plain-Language FinOps Explanation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FINOPS REASONING & RISK ASSESSMENT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (finding.explanation?.source == "gemini") FinOpsCyanSecondary.copy(alpha = 0.2f) else FinOpsEmeraldPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (finding.explanation?.source == "gemini") "GEMINI AI" else "DETERMINISTIC",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (finding.explanation?.source == "gemini") FinOpsCyanSecondary else FinOpsEmeraldPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            finding.explanation?.let { exp ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, FinOpsOutlineVariant, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    ExplanationSection("Summary", exp.summary, Icons.Default.Info, FinOpsCyanSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExplanationSection("Why It Matters", exp.whyItMatters, Icons.Default.CheckCircle, FinOpsEmeraldPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExplanationSection("Recommended Fix", exp.recommendedFix, Icons.Default.AutoAwesome, Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.height(8.dp))
                    ExplanationSection("Risk Assessment", exp.riskNote, Icons.Default.Warning, FinOpsAmberWarning)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "If Firebase AI is configured, requesting Gemini sends this resource's name, type, location, and observed inventory evidence to Google. Without Firebase, on-device guidance is used. Review guidance with the resource owner before acting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Optional hosted explanation; local guidance remains available offline.
            OutlinedButton(
                onClick = { onRequestAi(finding) },
                enabled = !isAiLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FinOpsCyanSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("request_gemini_ai_button")
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = FinOpsCyanSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating explanation…")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate explanation")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Example command only; never execute automatically.
            Text(
                text = "EXAMPLE REMEDIATION COMMAND · REVIEW BEFORE USE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070B12))
                    .border(1.dp, FinOpsOutline, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = finding.action.command,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF67E8F9),
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Copy Action Button
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Read-only cloud review command", finding.action.command)
                    clipboard.setPrimaryClip(clip)
                    onCopyCommand(finding.action.command)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FinOpsEmeraldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_remediation_command_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Copy Read-Only Review Command", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExplanationSection(title: String, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
