package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.models.Finding
import com.example.data.models.Severity
import com.example.ui.theme.FinOpsAmberWarning
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsOutline
import com.example.ui.theme.FinOpsRubyCritical

@Composable
fun FindingCard(
    finding: Finding,
    onClick: () -> Unit,
    onCopiedCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (severityColor, severityBg) = when (finding.severity) {
        Severity.HIGH -> FinOpsRubyCritical to FinOpsRubyCritical.copy(alpha = 0.15f)
        Severity.MEDIUM -> FinOpsAmberWarning to FinOpsAmberWarning.copy(alpha = 0.15f)
        Severity.LOW -> FinOpsCyanSecondary to FinOpsCyanSecondary.copy(alpha = 0.15f)
    }
    val typeIcon = when (finding.type) {
        "unattached_disk" -> Icons.Default.Storage
        "unused_ip" -> Icons.Default.Wifi
        "stopped_vm" -> Icons.Default.Dns
        else -> Icons.Default.Storage
    }
    val observedDetails = finding.evidence.metrics.entries.joinToString(" · ") { (key, value) ->
        "${key.replace('_', ' ')}: $value"
    }.ifBlank { "Inventory finding · ${finding.location}" }

    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .border(1.dp, FinOpsOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).testTag("finding_item_${finding.id}"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(typeIcon, contentDescription = finding.type, tint = FinOpsCyanSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(finding.type.replace('_', ' ').uppercase(), style = MaterialTheme.typography.labelSmall,
                            color = FinOpsCyanSecondary, fontWeight = FontWeight.Bold)
                        Text("GCP · ${finding.location}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(finding.severity.label.uppercase(), modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(severityBg).padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall, color = severityColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(finding.resourceName, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(observedDetails, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Surface(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Read-only cloud review command", finding.action.command))
                    onCopiedCommand(finding.action.command)
                },
                shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.End).testTag("copy_command_button_${finding.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy read-only review command",
                        modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("Copy review", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
