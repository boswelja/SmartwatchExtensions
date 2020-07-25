/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.watchmanager.BoolPreference
import com.boswelja.devicemanager.watchmanager.IntPreference
import com.boswelja.devicemanager.watchmanager.Watch

@Database(entities = [Watch::class, IntPreference::class, BoolPreference::class], version = 5)
abstract class WatchDatabase : RoomDatabase() {

    abstract fun watchDao(): WatchDao
    abstract fun intPrefDao(): IntPreferenceDao
    abstract fun boolPrefDao(): BoolPreferenceDao

    /**
     * Updates a stored preference value for a given preference and watch.
     * @param watchId The ID of the [Watch] whose preference we're updating.
     * @param preferenceKey The key for the preference to update.
     * @param newValue The new value of the preference to update.
     * @return true if the preference was successfully updated, false otherwise.
     */
    fun updatePrefInDatabase(
        watchId: String,
        preferenceKey: String,
        newValue: Any
    ): Boolean {
        if (isOpen) {
            return when (newValue) {
                is Boolean -> {
                    BoolPreference(watchId, preferenceKey, newValue).also {
                        boolPrefDao().update(it)
                    }
                    true
                }
                is Int -> {
                    IntPreference(watchId, preferenceKey, newValue).also {
                        intPrefDao().update(it)
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    /**
     * Gets a list of all registered watches with their preferences loaded.
     * @return The [List] of [Watch] objects that have all their preferences.
     * Will be null if the database is not open.
     */
    fun getRegisteredWatchesWithPrefs(): List<Watch>? {
        if (isOpen) {
            val watches = watchDao().getAll()
            for (watch in watches) {
                val boolPrefs = boolPrefDao().getAllForWatch(watch.id)
                val intPrefs = intPrefDao().getAllForWatch(watch.id)
                for (intPreference in intPrefs) {
                    watch.intPrefs[intPreference.key] = intPreference.value
                }
                for (boolPreference in boolPrefs) {
                    watch.boolPrefs[boolPreference.key] = boolPreference.value
                }
            }
            return watches
        } else {
            return null
        }
    }

    companion object {
        private var INSTANCE: WatchDatabase? = null

        /**
         * Gets an instance of [WatchDatabase].
         * @param context [Context].
         */
        fun get(context: Context): WatchDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db").apply {
                        addMigrations(Migrations.MIGRATION_3_5, Migrations.MIGRATION_4_5)
                        fallbackToDestructiveMigration()
                    }.build()
                }
                return INSTANCE!!
            }
        }
    }
}
