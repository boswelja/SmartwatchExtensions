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
    abstract fun intPreferenceDao(): IntPreferenceDao
    abstract fun boolPreferenceDao(): BoolPreferenceDao

    /**
     * Add a new [Watch] to the database.
     * @param watch The [Watch] to add.
     * @return true if the [Watch] was successfully added, false otherwise.
     */
    fun addWatch(watch: Watch): Boolean {
        if (isOpen) {
            watchDao().add(watch)
            return true
        }
        return false
    }

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
                        boolPreferenceDao().update(it)
                    }
                    true
                }
                is Int -> {
                    IntPreference(watchId, preferenceKey, newValue).also {
                        intPreferenceDao().update(it)
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
                val boolPrefs = boolPreferenceDao().getAllForWatch(watch.id)
                val intPrefs = intPreferenceDao().getAllForWatch(watch.id)
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

        /**
         * Opens a new instance of a [WatchDatabase].
         * @param context [Context].
         * @param allowMainThreadQueries Whether main thread queries should be allowed.
         * Set to true if the main thread is not the UI thread to allow synchronous calls.
         */
        fun open(context: Context, allowMainThreadQueries: Boolean = false): WatchDatabase =
            Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db").apply {
                if (allowMainThreadQueries) allowMainThreadQueries()
                addMigrations(Migrations.MIGRATION_3_5, Migrations.MIGRATION_4_5)
                fallbackToDestructiveMigration()
            }.build()
    }
}
