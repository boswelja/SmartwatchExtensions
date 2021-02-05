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
import androidx.room.TypeConverters
import com.boswelja.devicemanager.common.RoomTypeConverters
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.watchmanager.item.BoolPreference
import com.boswelja.devicemanager.watchmanager.item.IntPreference
import com.boswelja.devicemanager.watchmanager.item.Preference
import com.boswelja.devicemanager.watchmanager.item.Watch
import timber.log.Timber

@Database(entities = [Watch::class, IntPreference::class, BoolPreference::class], version = 7)
@TypeConverters(RoomTypeConverters::class)
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
    fun updatePrefInDatabase(watchId: String, preferenceKey: String, newValue: Any): Boolean {
        if (isOpen) {
            return when (newValue) {
                is Boolean -> {
                    BoolPreference(watchId, preferenceKey, newValue).also {
                        boolPrefDao().update(it)
                    }
                    true
                }
                is Int -> {
                    IntPreference(watchId, preferenceKey, newValue).also { intPrefDao().update(it) }
                    true
                }
                else -> false
            }
        }
        return false
    }

    fun renameWatch(watch: Watch, newName: String) {
        watchDao().setName(watch.id, newName)
    }

    fun forgetWatch(watch: Watch) {
        clearWatchPreferences(watch)
        watchDao().remove(watch.id)
    }

    fun clearWatchPreferences(watch: Watch) {
        intPrefDao().deleteAllForWatch(watch.id)
        boolPrefDao().deleteAllForWatch(watch.id)
    }

    fun addWatch(watch: Watch) {
        watchDao().add(watch)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getPreference(watch: Watch, key: String): Preference<T>? {
        return when (T::class) {
            Int::class -> intPrefDao().get(watch.id, key) as Preference<T>?
            Boolean::class -> boolPrefDao().get(watch.id, key) as Preference<T>?
            else -> {
                Timber.w("Tried to get preference for unsupported type ${T::class}")
                null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllPreferences(watch: Watch): List<Preference<Any>> {
        val intPrefs = intPrefDao().getAllForWatch(watch.id) as List<Preference<Any>>
        val boolPrefs = boolPrefDao().getAllForWatch(watch.id) as List<Preference<Any>>
        return intPrefs + boolPrefs
    }

    companion object : SingletonHolder<WatchDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db")
            .apply {
                addMigrations(
                    Migrations.MIGRATION_3_5,
                    Migrations.MIGRATION_4_5,
                    Migrations.MIGRATION_5_6,
                    Migrations.MIGRATION_6_7
                )
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
