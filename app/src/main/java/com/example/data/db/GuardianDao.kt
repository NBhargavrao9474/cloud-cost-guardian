package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianDao {
    @Query("SELECT * FROM scan_records ORDER BY startedAt DESC")
    fun getAllScanRecords(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records WHERE scanId = :scanId LIMIT 1")
    suspend fun getScanRecordById(scanId: String): ScanRecordEntity?

    @Query("SELECT * FROM findings WHERE scanId = :scanId ORDER BY severity DESC, resourceName ASC")
    fun getFindingsForScan(scanId: String): Flow<List<FindingEntity>>

    @Query("SELECT * FROM findings ORDER BY detectedAt DESC")
    fun getAllFindings(): Flow<List<FindingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(record: ScanRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<FindingEntity>)

    @Query("DELETE FROM findings WHERE scanId = :scanId")
    suspend fun deleteFindingsForScan(scanId: String)

    @Query("DELETE FROM scan_records WHERE scanId = :scanId")
    suspend fun deleteScanRecord(scanId: String)

    @Query("DELETE FROM scan_records")
    suspend fun clearAllScanRecords()

    @Query("DELETE FROM findings")
    suspend fun clearAllFindings()

    @Query("DELETE FROM findings WHERE scanId IN (SELECT scanId FROM scan_records WHERE status != 'live_gcp_read_only')")
    suspend fun deleteNonLiveFindings()

    @Query("DELETE FROM scan_records WHERE status != 'live_gcp_read_only'")
    suspend fun deleteNonLiveScanRecords()
}
