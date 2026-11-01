package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ScanStatus
import com.example.ui.theme.FinOpsCardGradientEnd
import com.example.ui.theme.FinOpsCardGradientStart
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsOutlineVariant

@Composable
fun FinOpsHeroCard(
    projectName: String,
    hasScanResults: Boolean,
    findingCount: Int,
    scanStatus: ScanStatus,
    onConnectOrScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(FinOpsEmeraldPrimary.copy(alpha = 0.6f), FinOpsCyanSecondary.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("hero_finops_card"),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(FinOpsCardGradientStart, FinOpsCardGradientEnd)))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(FinOpsEmeraldPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = FinOpsEmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(
                    text = projectName.ifBlank { "Google Cloud not connected" },
                    style = MaterialTheme.typography.labelMedium,
                    color = FinOpsEmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = if (hasScanResults) "COMPUTE ENGINE INVENTORY" else "LIVE CLOUD INVENTORY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (hasScanResults) "$findingCount resources for review" else "Find resources that need attention",
                style = MaterialTheme.typography.headlineSmall,
                color = FinOpsEmeraldPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Read-only inventory checks only. Verified costs and savings require billing data, which this app does not access.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onConnectOrScan,
                enabled = scanStatus != ScanStatus.SCANNING,
                colors = ButtonDefaults.buttonColors(containerColor = FinOpsEmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("live_gcp_scan_button")
            ) {
                if (scanStatus == ScanStatus.SCANNING) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Icon(
                        imageVector = if (hasScanResults) Icons.Default.Refresh else Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = when {
                        scanStatus == ScanStatus.SCANNING -> "Scanning Google Cloud…"
                        projectName.isBlank() -> "Connect Google Cloud · Read-only"
                        hasScanResults -> "Scan Google Cloud again"
                        else -> "Connect Google Cloud · Read-only"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
