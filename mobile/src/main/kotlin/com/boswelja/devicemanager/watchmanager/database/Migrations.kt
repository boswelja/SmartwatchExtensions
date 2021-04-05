package com.boswelja.devicemanager.watchmanager.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface

object Migrations {

    val MIGRATION_3_5 = object : Migration(3, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE new_watches (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "battery_sync_worker_id TEXT)"
            )
            database.execSQL("INSERT INTO new_watches (id, name) SELECT id, name FROM watches")
            database.execSQL("DROP TABLE watches")
            database.execSQL("ALTER TABLE new_watches RENAME TO watches")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE new_watches (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "battery_sync_worker_id TEXT)"
            )
            database.execSQL("INSERT INTO new_watches (id, name) SELECT id, name FROM watches")
            database.execSQL("DROP TABLE watches")
            database.execSQL("ALTER TABLE new_watches RENAME TO watches")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE new_watches (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL)"
            )
            database.execSQL("INSERT INTO new_watches (id, name) SELECT id, name FROM watches")
            database.execSQL("DROP TABLE watches")
            database.execSQL("ALTER TABLE new_watches RENAME TO watches")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE watches " +
                    "ADD platform TEXT NOT NULL DEFAULT ${WearOSConnectionInterface.PLATFORM}"
            )
        }
    }
}
