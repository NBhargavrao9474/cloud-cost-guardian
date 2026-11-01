package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.example.ai.ExplanationService
import com.example.data.db.AppDatabase
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import com.example.data.models.Severity
import com.example.data.repository.GuardianRepository
import com.example.engine.GcpComputeScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanStatus { IDLE, SCANNING, COMPLETED }

data class GuardianUiState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val projectName: String = "",
    val findings: List<Finding> = emptyList(),
    val scanHistory: List<ScanSummary> = emptyList(),
    val hasScanResults: Boolean = false,
    val isSavedScan: Boolean = false,
    val severityFilter: Severity? = null,
    val searchQuery: String = "",
    val terminalLogs: List<String> = emptyList(),
    val selectedFinding: Finding? = null,
    val showShareDialog: Boolean = false,
    val showProjectDialog: Boolean = false,
    val isAiLoadingFindingId: String? = null,
    val toastMessage: String? = null,
    val isDemoMode: Boolean = false
) {
    val filteredFindings: List<Finding>
        get() = findings.filter { finding ->
            val matchesSeverity = severityFilter == null || finding.severity == severityFilter
            val matchesSearch = searchQuery.isBlank() ||
                finding.resourceName.contains(searchQuery, ignoreCase = true) ||
                finding.type.contains(searchQuery, ignoreCase = true) ||
                finding.location.contains(searchQuery, ignoreCase = true)
            matchesSeverity && matchesSearch
        }
}

class GuardianViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GuardianRepository(AppDatabase.getInstance(application))
    private val _uiState = MutableStateFlow(GuardianUiState())
    val uiState: StateFlow<GuardianUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Purge simulated scan records and the local PIN state from earlier prototype builds.
            repository.purgeNonLiveHistory()
            getApplication<Application>()
                .getSharedPreferences("guardian_security_prefs", Context.MODE_PRIVATE)
                .edit { clear() }
            repository.allScanRecords.collect { records ->
                _uiState.update { it.copy(scanHistory = records) }
            }
        }
    }

    fun startLiveGcpScan(projectId: String, accessToken: String) {
        if (_uiState.value.scanStatus == ScanStatus.SCANNING) return
        viewModelScope.launch {
            val previousState = _uiState.value
            _uiState.update {
                it.copy(
                    scanStatus = ScanStatus.SCANNING,
                    projectName = projectId,
                    isDemoMode = false,
                    findings = emptyList(),
                    hasScanResults = false,
                    terminalLogs = emptyList(),
                    isSavedScan = false
                )
            }
            try {
                val (summary, findings) = GcpComputeScanner.runScan(
                    projectId = projectId,
                    accessToken = accessToken,
                    onLogMessage = { logLine ->
                        _uiState.update { current -> current.copy(terminalLogs = current.terminalLogs + logLine) }
                    }
                )
                repository.saveScan(summary, findings)
                _uiState.update {
                    it.copy(
                        scanStatus = ScanStatus.COMPLETED,
                        findings = findings,
                        hasScanResults = true,
                        isSavedScan = false,
                        toastMessage = "Live GCP inventory retrieved. Cost and billing data are not connected."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMessage = e.message ?: "GCP scan failed. Check project access and try again."
                _uiState.update {
                    previousState.copy(
                        projectName = projectId,
                        toastMessage = errorMessage,
                        terminalLogs = listOf("[ERROR] $errorMessage")
                    )
                }
            }
        }
    }

    fun restoreScan(summary: ScanSummary) {
        if (summary.status != "live_gcp_read_only") return
        viewModelScope.launch {
            val findings = repository.getFindingsForScan(summary.scanId).first()
            _uiState.update {
                it.copy(
                    projectName = summary.projectName,
                    isDemoMode = false,
                    findings = findings,
                    hasScanResults = true,
                    isSavedScan = true,
                    scanStatus = ScanStatus.COMPLETED,
                    terminalLogs = emptyList(),
                    toastMessage = "Showing saved inventory from ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(summary.startedAt))}. Run a new scan to refresh it."
                )
            }
        }
    }

    fun setSeverityFilter(severity: Severity?) = _uiState.update { it.copy(severityFilter = severity) }
    fun loadIllustrativeDemo() = _uiState.update {
        it.copy(
            projectName = "ILLUSTRATIVE DEMO · NOT LIVE GCP",
            findings = DemoFindings.items,
            hasScanResults = true,
            isSavedScan = false,
            isDemoMode = true,
            scanStatus = ScanStatus.COMPLETED,
            severityFilter = null,
            searchQuery = "",
            terminalLogs = emptyList(),
            selectedFinding = null,
            toastMessage = "Illustrative demo data loaded. No cloud project was scanned or changed."
        )
    }
    fun exitIllustrativeDemo() = _uiState.update {
        it.copy(
            projectName = "",
            findings = emptyList(),
            hasScanResults = false,
            isSavedScan = false,
            isDemoMode = false,
            scanStatus = ScanStatus.IDLE,
            severityFilter = null,
            searchQuery = "",
            selectedFinding = null,
            showShareDialog = false
        )
    }
    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun selectFinding(finding: Finding?) = _uiState.update { it.copy(selectedFinding = finding) }
    fun toggleShareDialog(show: Boolean) = _uiState.update { it.copy(showShareDialog = show) }
    fun toggleProjectDialog(show: Boolean) = _uiState.update { it.copy(showProjectDialog = show) }
    fun resetCurrentProject() = _uiState.update {
        it.copy(
            projectName = "",
            findings = emptyList(),
            hasScanResults = false,
            isDemoMode = false,
            isSavedScan = false,
            scanStatus = ScanStatus.IDLE,
            terminalLogs = emptyList(),
            selectedFinding = null,
            showShareDialog = false
        )
    }
    fun clearToast() = _uiState.update { it.copy(toastMessage = null) }
    fun showToast(message: String) = _uiState.update { it.copy(toastMessage = message) }

    fun requestAiInsight(finding: Finding) {
        if (_uiState.value.isAiLoadingFindingId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoadingFindingId = finding.id) }
            try {
                val explanation = ExplanationService.getEnhancedExplanation(finding)
                _uiState.update { current ->
                    val updated = current.findings.map { if (it.id == finding.id) it.copy(explanation = explanation) else it }
                    val selected = current.selectedFinding?.takeIf { it.id == finding.id }?.copy(explanation = explanation)
                    current.copy(
                        findings = updated,
                        selectedFinding = selected ?: current.selectedFinding,
                        toastMessage = if (explanation.source == "gemini") "Gemini explanation ready." else "On-device guidance ready."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(toastMessage = "Could not generate guidance. Please try again.") }
            } finally {
                _uiState.update { state ->
                    if (state.isAiLoadingFindingId == finding.id) state.copy(isAiLoadingFindingId = null) else state
                }
            }
        }
    }
}
