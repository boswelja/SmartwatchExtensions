package com.boswelja.devicemanager.watchconnman

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Watch::class], version = 1)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao
}