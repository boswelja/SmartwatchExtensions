package com.boswelja.smartwatchextensions.watchmanager.settings

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Database(entities = [DbIntSetting::class, DbBoolSetting::class], version = 2)
abstract class WatchSettingsDatabase : RoomDatabase() {
    abstract fun intSettings(): IntSettingDao
    abstract fun boolSettings(): BoolSettingDao

    /**
     * Updates a stored setting for a given key and watch.
     * @param watchId The ID of the watch whose setting we're updating.
     * @param key The key for the setting to update.
     * @param newValue The new value of the setting.
     * @return true if the setting was successfully updated, false otherwise.
     */
    suspend fun updateSetting(
        watchId: String,
        key: String,
        newValue: Any
    ): Boolean {
        if (isOpen) {
            return when (newValue) {
                is Boolean -> {
                    DbBoolSetting(watchId, key, newValue).also {
                        boolSettings().update(it)
                    }
                    true
                }
                is Int -> {
                    DbIntSetting(watchId, key, newValue).also {
                        intSettings().update(it)
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    /**
     * Update a setting with the given key for all watches that have it set.
     * @param key The setting key.
     * @param value The new value.
     */
    suspend fun updateAllForKey(
        key: String,
        value: Any
    ): Boolean {
        if (isOpen) {
            return when (value) {
                is Boolean -> {
                    boolSettings().updateByKey(key, value)
                    true
                }
                is Int -> {
                    intSettings().updateByKey(key, value)
                    true
                }
                else -> false
            }
        }
        return false
    }

    /**
     * Flow a Int setting for the given watch.
     * @param watchId The ID of the watch.
     * @param key The setting key.
     * @param defaultValue The value to emit when there is no value stored.
     */
    fun getIntSetting(
        watchId: String,
        key: String,
        defaultValue: Int = 0
    ): Flow<Int> = intSettings().get(watchId, key).map { it?.value ?: defaultValue }

    /**
     * Flow a Boolean setting for the given watch.
     * @param watchId The ID of the watch.
     * @param key The setting key.
     * @param defaultValue The value to emit when there is no value stored.
     */
    fun getBoolSetting(
        watchId: String,
        key: String,
        defaultValue: Boolean = false
    ): Flow<Boolean> = boolSettings().get(watchId, key).map { it?.value ?: defaultValue }

    /**
     * Removes all settings stored for the watch with the given ID.
     * @param watchId The watch ID to remove settings for.
     */
    suspend fun removeSettingsFor(watchId: String) {
        intSettings().deleteAllForWatch(watchId)
        boolSettings().deleteAllForWatch(watchId)
    }

    companion object {
        fun create(context: Context): WatchSettingsDatabase {
            return Room.databaseBuilder(context, WatchSettingsDatabase::class.java, "pref-db")
                .apply {
                    fallbackToDestructiveMigration()
                }
                .build()
        }
    }
}
