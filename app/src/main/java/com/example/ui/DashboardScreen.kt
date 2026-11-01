package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Severity
import com.example.ui.components.AnalyticsCharts
import com.example.ui.components.FindingCard
import com.example.ui.components.FindingDetailSheet
import com.example.ui.components.FinOpsHeroCard
import com.example.ui.components.ProjectInputDialog
import com.example.ui.components.ScanHistoryCard
import com.example.ui.components.ShareableAuditCardDialog
import com.example.ui.components.TerminalLogView
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsObsidianBg
import com.example.ui.theme.FinOpsOutlineVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: GuardianViewModel,
    modifier: Modifier = Modifier,
    onAuthorizeGcp: (String) -> Unit = {},
    onDisconnectGcp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinOpsObsidianBg,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(FinOpsEmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null,
                                tint = FinOpsEmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Cost Guardian", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Read-only GCP inventory · no billing data",
                                style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleProjectDialog(true) },
                        modifier = Modifier.testTag("switch_project_icon_btn")) {
                        Icon(Icons.Default.Cloud, contentDescription = "Connect or change Google Cloud project",
                            tint = FinOpsCyanSecondary)
                    }
                    if (uiState.hasScanResults) {
                        IconButton(onClick = { viewModel.toggleShareDialog(true) },
                            modifier = Modifier.testTag("open_share_dialog_icon_btn")) {
                            Icon(Icons.Default.Share, contentDescription = "Share inventory report",
                                tint = FinOpsEmeraldPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090E17))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(color = if (uiState.hasScanResults) Color(0xFF102A2A) else Color(0xFF142033),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            when {
                                uiState.isDemoMode -> "ILLUSTRATIVE DEMO DATA · NOT A LIVE SCAN"
                                !uiState.hasScanResults -> "NO CLOUD DATA YET"
                                uiState.isSavedScan -> "SAVED GCP INVENTORY"
                                else -> "LIVE GCP INVENTORY"
                            },
                            color = FinOpsCyanSecondary, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            when {
                                uiState.isDemoMode -> "Fictional examples for judging. These findings are not from Google Cloud and are not saved to scan history."
                                !uiState.hasScanResults -> "Connect your Google Cloud project to inspect its Compute Engine resources. No inventory has been loaded."
                                uiState.isSavedScan -> "These are saved read-only inventory results and may be out of date. Run a fresh scan to update them."
                                else -> "Retrieved from Compute Engine with read-only access. Billing APIs are not accessed; no cost or savings figures are reported."
                            },
                            color = Color(0xFFE2D3B5), style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                FinOpsHeroCard(
                    projectName = uiState.projectName,
                    hasScanResults = uiState.hasScanResults,
                    findingCount = uiState.findings.size,
                    scanStatus = uiState.scanStatus,
                    onConnectOrScan = {
                        if (uiState.projectName.isBlank()) viewModel.toggleProjectDialog(true)
                        else onAuthorizeGcp(uiState.projectName)
                    }
                )
            }

            item {
                if (uiState.isDemoMode) {
                    TextButton(
                        onClick = viewModel::exitIllustrativeDemo,
                        modifier = Modifier.fillMaxWidth().testTag("exit_illustrative_demo_button")
                    ) { Text("Exit demo and return to live mode") }
                } else if (!uiState.hasScanResults) {
                    TextButton(
                        onClick = viewModel::loadIllustrativeDemo,
                        modifier = Modifier.fillMaxWidth().testTag("load_illustrative_demo_button")
                    ) { Text("Try illustrative demo data") }
                }
            }

            if (uiState.scanHistory.isNotEmpty()) {
                item { ScanHistoryCard(scans = uiState.scanHistory, onSelect = viewModel::restoreScan) }
            }

            if (uiState.terminalLogs.isNotEmpty()) {
                item { TerminalLogView(logs = uiState.terminalLogs) }
            }

            if (uiState.hasScanResults && uiState.findings.isNotEmpty()) {
                item { AnalyticsCharts(findings = uiState.filteredFindings) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = { Text("Filter resources, zones, or IDs…", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                            trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                                { IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                } }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinOpsCyanSecondary,
                                unfocusedBorderColor = FinOpsOutlineVariant,
                                focusedContainerColor = Color(0xFF131B2A),
                                unfocusedContainerColor = Color(0xFF131B2A)
                            ),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_filter_text_field")
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = uiState.severityFilter == null,
                                onClick = { viewModel.setSeverityFilter(null) },
                                label = { Text("All (${uiState.findings.size})") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FinOpsEmeraldPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = FinOpsEmeraldPrimary)
                            )
                            listOf(Severity.HIGH, Severity.MEDIUM, Severity.LOW).forEach { severity ->
                                FilterChip(
                                    selected = uiState.severityFilter == severity,
                                    onClick = { viewModel.setSeverityFilter(if (uiState.severityFilter == severity) null else severity) },
                                    label = { Text(severity.label) }
                                )
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("COMPUTE ENGINE RESOURCES FOR REVIEW", style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${uiState.filteredFindings.size}", style = MaterialTheme.typography.labelSmall,
                            color = FinOpsEmeraldPrimary)
                    }
                }
                if (uiState.filteredFindings.isEmpty()) {
                    item {
                        Text(
                            if (uiState.findings.isEmpty()) "No review findings were returned by this inventory scan. This does not confirm that the project has no costs."
                            else "No resources match the current search and severity filter.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.filteredFindings, key = { it.id }) { finding ->
                        FindingCard(
                            finding = finding,
                            onClick = { viewModel.selectFinding(finding) },
                            onCopiedCommand = { viewModel.showToast("Copied read-only review command.") }
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (uiState.selectedFinding != null) {
        FindingDetailSheet(
            finding = uiState.selectedFinding,
            isAiLoading = uiState.isAiLoadingFindingId == uiState.selectedFinding?.id,
            onDismiss = { viewModel.selectFinding(null) },
            onRequestAi = viewModel::requestAiInsight,
            onCopyCommand = { viewModel.showToast("Copied read-only review command.") }
        )
    }
    if (uiState.showShareDialog && uiState.hasScanResults) {
        ShareableAuditCardDialog(
            projectName = uiState.projectName,
            findings = uiState.findings,
            onDismiss = { viewModel.toggleShareDialog(false) },
            isDemoMode = uiState.isDemoMode
        )
    }
    if (uiState.showProjectDialog) {
        ProjectInputDialog(
            currentProject = uiState.projectName,
            onDisconnect = onDisconnectGcp,
            onConfirm = { projectId ->
                viewModel.toggleProjectDialog(false)
                onAuthorizeGcp(projectId)
            },
            onDismiss = { viewModel.toggleProjectDialog(false) }
        )
    }
}
