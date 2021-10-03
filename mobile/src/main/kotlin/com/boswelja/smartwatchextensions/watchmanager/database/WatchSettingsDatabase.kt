package com.boswelja.smartwatchextensions.watchmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.smartwatchextensions.common.RoomTypeConverters
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.watchmanager.item.BoolSetting
import com.boswelja.smartwatchextensions.watchmanager.item.IntSetting

@Database(entities = [IntSetting::class, BoolSetting::class], version = 2)
@TypeConverters(RoomTypeConverters::class)
abstract class WatchSettingsDatabase : RoomDatabase() {
    abstract fun intSettings(): IntSettingDao
    abstract fun boolSettings(): BoolSettingDao

    /**
     * Updates a stored setting for a given key and watch.
     * @param watchId The ID of the [Watch] whose preference we're updating.
     * @param key The key for the preference to update.
     * @param newValue The new value of the preference to update.
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
                    BoolSetting(watchId, key, newValue).also {
                        boolSettings().update(it)
                    }
                    true
                }
                is Int -> {
                    IntSetting(watchId, key, newValue).also {
                        intSettings().update(it)
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    companion object : SingletonHolder<WatchSettingsDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchSettingsDatabase::class.java, "pref-db")
            .apply {
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
