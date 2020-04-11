/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.watchconnectionmanager.BoolPreference
import com.boswelja.devicemanager.watchconnectionmanager.IntPreference
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchPreferenceChangeInterface

@Database(entities = [Watch::class, IntPreference::class, BoolPreference::class], version = 5)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao

    abstract fun intPreferenceDao(): IntPreferenceDao
    abstract fun boolPreferenceDao(): BoolPreferenceDao

    fun addWatch(watch: Watch): Boolean {
        if (isOpen) {
            watchDao().add(watch)
            return true
        }
        return false
    }

    fun updatePrefInDatabase(id: String, key: String, newValue: Any, watchPreferenceChangeInterfaces: List<WatchPreferenceChangeInterface>? = null): Boolean {
        if (isOpen) {
            return when (newValue) {
                is Boolean -> {
                    val boolPreference = BoolPreference(id, key, newValue)
                    boolPreferenceDao().update(boolPreference)
                    if (watchPreferenceChangeInterfaces != null) {
                        for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                            watchPreferenceChangeInterface.boolPreferenceChanged(boolPreference)
                        }
                    }
                    true
                }
                is Int -> {
                    val intPreference = IntPreference(id, key, newValue)
                    intPreferenceDao().update(intPreference)
                    if (watchPreferenceChangeInterfaces != null) {
                        for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                            watchPreferenceChangeInterface.intPreferenceChanged(intPreference)
                        }
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    fun getWatchesWithPrefs(): List<Watch> {
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
    }

    companion object {
        fun open(context: Context, allowMainThreadQueries: Boolean = false): WatchDatabase =
                Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db").apply {
                    if (allowMainThreadQueries) allowMainThreadQueries()
                    addMigrations(Migrations.MIGRATION_3_5, Migrations.MIGRATION_4_5)
                    fallbackToDestructiveMigration()
                }.build()
    }
}
