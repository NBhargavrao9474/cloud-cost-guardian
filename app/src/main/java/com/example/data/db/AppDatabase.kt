package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScanRecordEntity::class, FindingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guardianDao(): GuardianDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE findings RENAME TO findings_v1")
                db.execSQL("""CREATE TABLE findings (
                    id TEXT NOT NULL, scanId TEXT NOT NULL, cloud TEXT NOT NULL,
                    checkId TEXT NOT NULL, type TEXT NOT NULL, resourceName TEXT NOT NULL,
                    resourceUrl TEXT, project TEXT NOT NULL, location TEXT NOT NULL,
                    severity TEXT NOT NULL, monthlyCostUsd REAL NOT NULL,
                    monthlySavingsUsd REAL NOT NULL, pricingBasis TEXT NOT NULL,
                    priceSource TEXT NOT NULL, actionKind TEXT NOT NULL,
                    actionCommand TEXT NOT NULL, actionReversible INTEGER NOT NULL,
                    actionRisk TEXT NOT NULL, evidenceMetricsJson TEXT NOT NULL,
                    evidenceNotesJson TEXT NOT NULL, observedDays INTEGER,
                    explanationSummary TEXT NOT NULL, explanationWhy TEXT NOT NULL,
                    explanationFix TEXT NOT NULL, explanationRisk TEXT NOT NULL,
                    explanationSource TEXT NOT NULL, detectedAt INTEGER NOT NULL,
                    PRIMARY KEY(scanId, id)
                )""".trimIndent())
                db.execSQL("INSERT INTO findings SELECT * FROM findings_v1")
                db.execSQL("DROP TABLE findings_v1")
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE scan_records_new (
                    scanId TEXT NOT NULL PRIMARY KEY, projectName TEXT NOT NULL,
                    status TEXT NOT NULL, startedAt INTEGER NOT NULL,
                    finishedAt INTEGER, findingsCount INTEGER NOT NULL
                )""".trimIndent())
                db.execSQL("""INSERT INTO scan_records_new
                    (scanId, projectName, status, startedAt, finishedAt, findingsCount)
                    SELECT scanId, projectName, status, startedAt, finishedAt, findingsCount FROM scan_records""".trimIndent())
                db.execSQL("DROP TABLE scan_records")
                db.execSQL("ALTER TABLE scan_records_new RENAME TO scan_records")

                db.execSQL("""CREATE TABLE findings_new (
                    id TEXT NOT NULL, scanId TEXT NOT NULL, cloud TEXT NOT NULL,
                    checkId TEXT NOT NULL, type TEXT NOT NULL, resourceName TEXT NOT NULL,
                    resourceUrl TEXT, project TEXT NOT NULL, location TEXT NOT NULL,
                    severity TEXT NOT NULL, actionKind TEXT NOT NULL,
                    actionCommand TEXT NOT NULL, actionReversible INTEGER NOT NULL,
                    actionRisk TEXT NOT NULL, evidenceMetricsJson TEXT NOT NULL,
                    evidenceNotesJson TEXT NOT NULL, observedDays INTEGER,
                    explanationSummary TEXT NOT NULL, explanationWhy TEXT NOT NULL,
                    explanationFix TEXT NOT NULL, explanationRisk TEXT NOT NULL,
                    explanationSource TEXT NOT NULL, detectedAt INTEGER NOT NULL,
                    PRIMARY KEY(scanId, id)
                )""".trimIndent())
                db.execSQL("""INSERT INTO findings_new (
                    id, scanId, cloud, checkId, type, resourceName, resourceUrl, project,
                    location, severity, actionKind, actionCommand, actionReversible, actionRisk,
                    evidenceMetricsJson, evidenceNotesJson, observedDays, explanationSummary,
                    explanationWhy, explanationFix, explanationRisk, explanationSource, detectedAt
                ) SELECT id, scanId, cloud, checkId, type, resourceName, resourceUrl, project,
                    location, severity, actionKind, actionCommand, actionReversible, actionRisk,
                    evidenceMetricsJson, evidenceNotesJson, observedDays, explanationSummary,
                    explanationWhy, explanationFix, explanationRisk, explanationSource, detectedAt
                    FROM findings""".trimIndent())
                db.execSQL("DROP TABLE findings")
                db.execSQL("ALTER TABLE findings_new RENAME TO findings")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cost_guardian_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
