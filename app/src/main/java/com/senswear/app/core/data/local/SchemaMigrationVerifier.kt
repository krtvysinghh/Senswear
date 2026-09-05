package com.senswear.app.core.data.local

import android.database.sqlite.SQLiteDatabase

/**
 * Validates database schema integrity and non-destructive column/table upgrades.
 */
class SchemaMigrationVerifier {

    fun verifyProvenanceTableExists(db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='data_provenance_records'",
            null
        )
        val exists = cursor.use { it.count > 0 }
        return exists
    }

    fun verifyCompositeIndices(db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='index_data_provenance_records_metric_name_timestamp'",
            null
        )
        return cursor.use { it.count > 0 }
    }
}
