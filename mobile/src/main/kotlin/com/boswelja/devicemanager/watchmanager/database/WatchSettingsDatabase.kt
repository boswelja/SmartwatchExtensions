package com.boswelja.devicemanager.watchmanager.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.watchmanager.item.BoolPreference
import com.boswelja.devicemanager.watchmanager.item.IntPreference
import com.boswelja.devicemanager.watchmanager.item.Preference
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Database(entities = [IntPreference::class, BoolPreference::class], version = 1)
abstract class WatchSettingsDatabase : RoomDatabase() {
    abstract fun intPrefDao(): IntPreferenceDao
    abstract fun boolPrefDao(): BoolPreferenceDao

    suspend fun clearWatchPreferences(watch: Watch) {
        withContext(Dispatchers.getIO()) {
            intPrefDao().deleteAllForWatch(watch.id)
            boolPrefDao().deleteAllForWatch(watch.id)
        }
    }

    /**
     * Updates a stored preference value for a given preference and watch.
     * @param watchId The ID of the [Watch] whose preference we're updating.
     * @param preferenceKey The key for the preference to update.
     * @param newValue The new value of the preference to update.
     * @return true if the preference was successfully updated, false otherwise.
     */
    suspend fun updatePrefInDatabase(
        watchId: String,
        preferenceKey: String,
        newValue: Any
    ): Boolean {
        return withContext(Dispatchers.getIO()) {
            if (isOpen) {
                return@withContext when (newValue) {
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
            return@withContext false
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend inline fun <reified T> getPreference(watchId: String, key: String): Preference<T>? {
        return withContext(Dispatchers.getIO()) {
            return@withContext when (T::class) {
                Int::class -> intPrefDao().get(watchId, key) as Preference<T>?
                Boolean::class -> boolPrefDao().get(watchId, key) as Preference<T>?
                else -> {
                    Timber.w("Tried to get preference for unsupported type ${T::class}")
                    null
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getPreferenceObservable(
        watchId: String,
        key: String
    ): LiveData<Preference<T>?>? {
        return when (T::class) {
            Int::class -> intPrefDao().getObservable(watchId, key) as LiveData<Preference<T>?>
            Boolean::class ->
                boolPrefDao().getObservable(watchId, key) as LiveData<Preference<T>?>
            else -> {
                Timber.w("Tried to get preference for unsupported type ${T::class}")
                null
            }
        }
    }

    companion object : SingletonHolder<WatchSettingsDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchSettingsDatabase::class.java, "pref-db")
            .apply {
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
