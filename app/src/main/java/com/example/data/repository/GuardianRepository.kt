package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.GuardianConverters
import com.example.data.db.ScanRecordEntity
import androidx.room.withTransaction
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GuardianRepository(private val database: AppDatabase) {
    private val dao = database.guardianDao()

    val allScanRecords: Flow<List<ScanSummary>> = dao.getAllScanRecords().map { list ->
        list.map { entity ->
            ScanSummary(
                scanId = entity.scanId,
                projectName = entity.projectName,
                status = entity.status,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
                findingsCount = entity.findingsCount
            )
        }
    }

    val latestFindings: Flow<List<Finding>> = dao.getAllFindings().map { list ->
        list.map { GuardianConverters.toFinding(it) }
    }

    fun getFindingsForScan(scanId: String): Flow<List<Finding>> {
        return dao.getFindingsForScan(scanId).map { list ->
            list.map { GuardianConverters.toFinding(it) }
        }
    }

    suspend fun saveScan(scanSummary: ScanSummary, findings: List<Finding>) {
        val recordEntity = ScanRecordEntity(
            scanId = scanSummary.scanId,
            projectName = scanSummary.projectName,
            status = scanSummary.status,
            startedAt = scanSummary.startedAt,
            finishedAt = scanSummary.finishedAt,
            findingsCount = scanSummary.findingsCount
        )
        val findingEntities = findings.map { finding ->
            GuardianConverters.fromFinding(finding, scanSummary.scanId)
        }
        database.withTransaction {
            dao.insertScanRecord(recordEntity)
            dao.insertFindings(findingEntities)
        }
    }

    suspend fun deleteScan(scanId: String) {
        dao.deleteFindingsForScan(scanId)
        dao.deleteScanRecord(scanId)
    }

    suspend fun clearAll() {
        dao.clearAllFindings()
        dao.clearAllScanRecords()
    }

    suspend fun purgeNonLiveHistory() {
        database.withTransaction {
            dao.deleteNonLiveFindings()
            dao.deleteNonLiveScanRecords()
        }
    }
}
