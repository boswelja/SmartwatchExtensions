package com.boswelja.devicemanager.batterysync.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE watch_battery_stats ADD COLUMN watch_charging INTEGER NOT NULL DEFAULT 0")
        }
    }
}