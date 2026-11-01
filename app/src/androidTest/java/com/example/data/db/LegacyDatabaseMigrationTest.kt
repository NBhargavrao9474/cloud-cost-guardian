package com.example.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LegacyDatabaseMigrationTest {
    @Test
    fun migrationDropsPrototypePriceColumnsAndKeepsRealInventoryRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "guardian-migration-${UUID.randomUUID()}.db"
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseName)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""CREATE TABLE scan_records (
                        scanId TEXT NOT NULL PRIMARY KEY, projectName TEXT NOT NULL,
                        status TEXT NOT NULL, startedAt INTEGER NOT NULL,
                        finishedAt INTEGER, findingsCount INTEGER NOT NULL,
                        monthlySavingsUsd REAL NOT NULL DEFAULT 0, grade TEXT NOT NULL DEFAULT ''
                    )""")
                    db.execSQL("""CREATE TABLE findings (
                        id TEXT NOT NULL, scanId TEXT NOT NULL, cloud TEXT NOT NULL,
                        checkId TEXT NOT NULL, type TEXT NOT NULL, resourceName TEXT NOT NULL,
                        resourceUrl TEXT, project TEXT NOT NULL, location TEXT NOT NULL,
                        severity TEXT NOT NULL, monthlyCostUsd REAL NOT NULL DEFAULT 0,
                        monthlySavingsUsd REAL NOT NULL DEFAULT 0,
                        pricingBasis TEXT NOT NULL DEFAULT '', priceSource TEXT NOT NULL DEFAULT '',
                        actionKind TEXT NOT NULL, actionCommand TEXT NOT NULL,
                        actionReversible INTEGER NOT NULL, actionRisk TEXT NOT NULL,
                        evidenceMetricsJson TEXT NOT NULL, evidenceNotesJson TEXT NOT NULL,
                        observedDays INTEGER, explanationSummary TEXT NOT NULL,
                        explanationWhy TEXT NOT NULL, explanationFix TEXT NOT NULL,
                        explanationRisk TEXT NOT NULL, explanationSource TEXT NOT NULL,
                        detectedAt INTEGER NOT NULL, PRIMARY KEY(scanId, id)
                    )""")
                    db.execSQL("""INSERT INTO scan_records
                        (scanId, projectName, status, startedAt, finishedAt, findingsCount,
                         monthlySavingsUsd, grade)
                        VALUES ('old-scan', 'fixture-project', 'live_gcp_read_only', 10, 20, 1, 99, 'A')""")
                    db.execSQL("""INSERT INTO findings (
                        id, scanId, cloud, checkId, type, resourceName, project, location,
                        severity, actionKind, actionCommand, actionReversible, actionRisk,
                        evidenceMetricsJson, evidenceNotesJson, explanationSummary,
                        explanationWhy, explanationFix, explanationRisk, explanationSource, detectedAt
                    ) VALUES (
                        'old-finding', 'old-scan', 'GCP', 'unattached_disk', 'disk',
                        'disk-1', 'fixture-project', 'us-central1-a', 'MEDIUM', 'review',
                        'gcloud compute disks describe disk-1', 1, 'Review dependencies',
                        '{}', '[]', '', '', '', '', 'template', 20
                    )""")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()

        try {
            FrameworkSQLiteOpenHelperFactory().create(configuration).use { helper ->
                val db = helper.writableDatabase
                AppDatabase.MIGRATION_2_3.migrate(db)

                assertEquals(1, db.query("SELECT COUNT(*) FROM scan_records WHERE scanId = 'old-scan'").use {
                    assertTrue(it.moveToFirst())
                    it.getInt(0)
                })
                assertEquals(1, db.query("SELECT COUNT(*) FROM findings WHERE id = 'old-finding'").use {
                    assertTrue(it.moveToFirst())
                    it.getInt(0)
                })
                assertFalse(columnExists(db, "scan_records", "monthlySavingsUsd"))
                assertFalse(columnExists(db, "scan_records", "grade"))
                assertFalse(columnExists(db, "findings", "monthlyCostUsd"))
                assertFalse(columnExists(db, "findings", "monthlySavingsUsd"))
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    private fun columnExists(db: SupportSQLiteDatabase, table: String, column: String): Boolean =
        db.query("PRAGMA table_info($table)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return@use true
            }
            false
        }
}
