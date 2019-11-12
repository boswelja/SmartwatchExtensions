package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Watch::class, IntPreference::class, BoolPreference::class], version = 2)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao

    abstract fun intPreferenceDao(): IntPreferenceDao
    abstract fun boolPreferenceDao(): BoolPreferenceDao
}