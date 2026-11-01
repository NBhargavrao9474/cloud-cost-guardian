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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsOutlineVariant

@Composable
fun ProjectInputDialog(
    currentProject: String,
    onConfirm: (String) -> Unit,
    onDisconnect: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var text by remember(currentProject) { mutableStateOf(currentProject) }
    val validProjectId = Regex("^[a-z][a-z0-9-]{4,28}[a-z0-9]$").matches(text.trim())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .border(1.dp, FinOpsOutlineVariant, RoundedCornerShape(20.dp))
                .testTag("project_input_dialog"),
            color = androidx.compose.ui.graphics.Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = FinOpsCyanSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Connect Google Cloud", style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter the real Google Cloud project ID. Guardian requests temporary Compute Engine read-only access to list instances, disks, and addresses. No resources are modified. Billing data is not requested.",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Google Cloud project ID") },
                    singleLine = true,
                    supportingText = { Text("6–30 lowercase letters, digits, and hyphens") },
                    modifier = Modifier.fillMaxWidth().testTag("project_id_input_field")
                )
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onConfirm(text.trim()) },
                        enabled = validProjectId,
                        colors = ButtonDefaults.buttonColors(containerColor = FinOpsEmeraldPrimary,
                            contentColor = androidx.compose.ui.graphics.Color.Black),
                        modifier = Modifier.weight(1f).testTag("start_custom_project_scan_button")
                    ) { Text("Authorize & scan", fontWeight = FontWeight.Bold) }
                }
                TextButton(onClick = { onDisconnect(); onDismiss() }, modifier = Modifier.align(Alignment.End)) {
                    Text("Disconnect Google Cloud access")
                }
            }
        }
    }
}
