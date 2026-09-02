package com.senswear.app.core.data.local

import android.database.sqlite.SQLiteDatabase

object DatabaseTransactionUtils {
    inline fun <T> runInTransaction(db: SQLiteDatabase, block: (SQLiteDatabase) -> T): T {
        db.beginTransaction()
        return try {
            val result = block(db)
            db.setTransactionSuccessful()
            result
        } finally {
            db.endTransaction()
        }
    }
}
