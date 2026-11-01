package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.models.Evidence
import com.example.data.models.Explanation
import com.example.data.models.Finding
import com.example.data.models.Severity
import com.example.data.models.SuggestedAction
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey val scanId: String,
    val projectName: String,
    val status: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val findingsCount: Int
)

@Entity(tableName = "findings", primaryKeys = ["scanId", "id"])
data class FindingEntity(
    val id: String,
    val scanId: String,
    val cloud: String,
    val checkId: String,
    val type: String,
    val resourceName: String,
    val resourceUrl: String?,
    val project: String,
    val location: String,
    val severity: String,
    val actionKind: String,
    val actionCommand: String,
    val actionReversible: Boolean,
    val actionRisk: String,
    val evidenceMetricsJson: String,
    val evidenceNotesJson: String,
    val observedDays: Int?,
    val explanationSummary: String,
    val explanationWhy: String,
    val explanationFix: String,
    val explanationRisk: String,
    val explanationSource: String,
    val detectedAt: Long
)

class GuardianConverters {
    companion object {
        fun toFinding(entity: FindingEntity): Finding {
            val metricsMap = mutableMapOf<String, String>()
            try {
                if (entity.evidenceMetricsJson.isNotEmpty()) {
                    val json = JSONObject(entity.evidenceMetricsJson)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        metricsMap[key] = json.optString(key, "")
                    }
                }
            } catch (_: Exception) {}

            val notesList = mutableListOf<String>()
            try {
                if (entity.evidenceNotesJson.isNotEmpty()) {
                    val arr = JSONArray(entity.evidenceNotesJson)
                    for (i in 0 until arr.length()) {
                        notesList.add(arr.getString(i))
                    }
                }
            } catch (_: Exception) {}

            return Finding(
                id = entity.id,
                cloud = entity.cloud,
                checkId = entity.checkId,
                type = entity.type,
                resourceName = entity.resourceName,
                resourceUrl = entity.resourceUrl,
                project = entity.project,
                location = entity.location,
                severity = when (entity.severity.uppercase()) {
                    "HIGH" -> Severity.HIGH
                    "MEDIUM" -> Severity.MEDIUM
                    else -> Severity.LOW
                },
                evidence = Evidence(
                    metrics = metricsMap,
                    observedDays = entity.observedDays,
                    notes = notesList
                ),
                action = SuggestedAction(
                    kind = entity.actionKind,
                    command = entity.actionCommand,
                    reversible = entity.actionReversible,
                    risk = entity.actionRisk
                ),
                explanation = Explanation(
                    summary = entity.explanationSummary,
                    whyItMatters = entity.explanationWhy,
                    recommendedFix = entity.explanationFix,
                    riskNote = entity.explanationRisk,
                    source = entity.explanationSource
                ),
                detectedAt = entity.detectedAt
            )
        }

        fun fromFinding(finding: Finding, scanId: String): FindingEntity {
            val metricsJson = JSONObject()
            finding.evidence.metrics.forEach { (k, v) -> metricsJson.put(k, v) }
            val notesJson = JSONArray()
            finding.evidence.notes.forEach { notesJson.put(it) }

            return FindingEntity(
                id = finding.id,
                scanId = scanId,
                cloud = finding.cloud,
                checkId = finding.checkId,
                type = finding.type,
                resourceName = finding.resourceName,
                resourceUrl = finding.resourceUrl,
                project = finding.project,
                location = finding.location,
                severity = finding.severity.name,
                actionKind = finding.action.kind,
                actionCommand = finding.action.command,
                actionReversible = finding.action.reversible,
                actionRisk = finding.action.risk,
                evidenceMetricsJson = metricsJson.toString(),
                evidenceNotesJson = notesJson.toString(),
                observedDays = finding.evidence.observedDays,
                explanationSummary = finding.explanation?.summary ?: "",
                explanationWhy = finding.explanation?.whyItMatters ?: "",
                explanationFix = finding.explanation?.recommendedFix ?: "",
                explanationRisk = finding.explanation?.riskNote ?: "",
                explanationSource = finding.explanation?.source ?: "template",
                detectedAt = finding.detectedAt
            )
        }
    }
}
