package com.example.data.models

enum class Severity(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class Evidence(
    val metrics: Map<String, String> = emptyMap(),
    val observedDays: Int? = null,
    val notes: List<String> = emptyList()
)

data class SuggestedAction(
    val kind: String,
    val command: String,
    val reversible: Boolean,
    val risk: String
)

data class Explanation(
    val summary: String,
    val whyItMatters: String,
    val recommendedFix: String,
    val riskNote: String,
    val source: String,        // "gemini" or "template"
    val model: String? = null
)

data class Finding(
    val id: String,
    val cloud: String,
    val checkId: String,
    val type: String,
    val resourceName: String,
    val resourceUrl: String? = null,
    val project: String,
    val location: String,
    val severity: Severity,
    val evidence: Evidence,
    val action: SuggestedAction,
    val explanation: Explanation? = null,
    val detectedAt: Long = System.currentTimeMillis()
)

data class ScanSummary(
    val scanId: String,
    val projectName: String,
    val status: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val findingsCount: Int = 0
)
