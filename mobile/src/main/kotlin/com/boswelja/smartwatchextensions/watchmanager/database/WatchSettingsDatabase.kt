package com.boswelja.smartwatchextensions.watchmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.smartwatchextensions.common.RoomTypeConverters
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.watchmanager.item.BoolPreference
import com.boswelja.smartwatchextensions.watchmanager.item.IntPreference
import com.boswelja.watchconnection.core.Watch
import java.util.UUID

@Database(entities = [IntPreference::class, BoolPreference::class], version = 1)
@TypeConverters(RoomTypeConverters::class)
abstract class WatchSettingsDatabase : RoomDatabase() {
    abstract fun intPrefDao(): IntPreferenceDao
    abstract fun boolPrefDao(): BoolPreferenceDao

    suspend fun clearWatchPreferences(watch: Watch) {
        intPrefDao().deleteAllForWatch(watch.id)
        boolPrefDao().deleteAllForWatch(watch.id)
    }

    /**
     * Updates a stored preference value for a given preference and watch.
     * @param watchId The ID of the [Watch] whose preference we're updating.
     * @param preferenceKey The key for the preference to update.
     * @param newValue The new value of the preference to update.
     * @return true if the preference was successfully updated, false otherwise.
     */
    suspend fun updatePrefInDatabase(
        watchId: UUID,
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

    companion object : SingletonHolder<WatchSettingsDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchSettingsDatabase::class.java, "pref-db")
            .apply {
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
