/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.BoolPreference
import com.boswelja.devicemanager.watchmanager.item.IntPreference
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for updating watch preferences both in the database and on the
 * target watch. The current preference structure loads preferences from the database into the
 * default [SharedPreferences] store, and updates them in the database as they're changed.
 */
class WatchPreferenceManager
internal constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val dataClient: DataClient = Wearable.getDataClient(context),
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context),
    private val database: WatchDatabase = WatchDatabase.getInstance(context),
    private val analytics: Analytics = Analytics(context)
) {

    /**
     * Clears all local [SharedPreferences], then reads the values stored in the [database] for a
     * specified [Watch.id] into the local [SharedPreferences].
     * @param watchId The [Watch.id] to query the database for corresponding preferences.
     */
    fun updateLocalPreferences(watchId: String) {
        Timber.d("updateLocalPreferences($watchId) called")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val boolPrefs = database.boolPrefDao().getAllForWatch(watchId)
                val intPrefs = database.intPrefDao().getAllForWatch(watchId)
                clearLocalPreferences(commitNow = true)
                sharedPreferences.edit {
                    boolPrefs.forEach {
                        Timber.i("Setting ${it.key} to ${it.value}")
                        putBoolean(it.key, it.value)
                    }
                    intPrefs.forEach {
                        Timber.i("Setting ${it.key} to ${it.value}")
                        putInt(it.key, it.value)
                    }
                }
            }
        }
    }

    /** Removes all watch-specific preferences from the local [SharedPreferences]. */
    fun clearLocalPreferences(commitNow: Boolean = false) {
        Timber.d("clearLocalPreferences($commitNow) called")
        sharedPreferences.edit(commit = commitNow) {
            SyncPreferences.ALL_PREFS.forEach { remove(it) }
        }
    }

    /**
     * Get a bool preference for a specified watch from the database.
     * @param watchId The ID of the watch to get the preference for.
     * @param key The requested preference key.
     * @return The corresponding bool, or false if it doesn't exist.
     */
    fun getBool(watchId: String, key: String) =
        database.boolPrefDao().get(watchId, key)?.value == true

    /**
     * Get an int preference for a specified watch from the database.
     * @param watchId The ID of the watch to get the preference for.
     * @param key The requested preference key.
     * @return The corresponding int, or null if it doesn't exist.
     */
    fun getInt(watchId: String, key: String) = database.intPrefDao().get(watchId, key)?.value

    /**
     * Update a given preference in the database for the connected watch.
     * @param preferenceKey The preference key to update.
     * @param newValue The new value of the preference.
     * @return true if the update was successful, false otherwise.
     */
    fun updatePreferenceInDatabase(watchId: String, preferenceKey: String, newValue: Any): Boolean {
        analytics.logSettingChanged(preferenceKey, newValue)
        return database.updatePrefInDatabase(watchId, preferenceKey, newValue)
    }

    /**
     * Clear all preferences for a given [Watch] from the database.
     * @param watchId The [Watch.id] to clear preferences for.
     * @return true if the preferences were successfully cleared, false otherwise.
     */
    suspend fun clearPreferencesForWatch(watchId: String?): Boolean {
        Timber.d("clearPreferencesForWatch($watchId) called")
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                Timber.i("Clearing watch preferences")
                database.intPrefDao().deleteAllForWatch(watchId)
                database.boolPrefDao().deleteAllForWatch(watchId)
                updateAllPreferencesOnWatch(watchId)
                return@withContext true
            } else {
                Timber.w("watchId null or empty, or database closed")
            }
            return@withContext false
        }
    }

    /**
     * Sends an updated preference to the specified [Watch].
     * @param watchId The [Watch.id] to send the preference to.
     * @param preferenceKey The key of the preference to update on the watch.
     * @return The [Task] for the preference send job, or null if the task failed.
     */
    suspend fun updatePreferenceOnWatch(watchId: String, preferenceKey: String): Task<DataItem>? {
        Timber.d("updatePreferenceOnWatch($preferenceKey) called")
        return withContext(Dispatchers.IO) {
            val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_$watchId")
            when (preferenceKey) {
                in SyncPreferences.BOOL_PREFS -> {
                    sharedPreferences.getBoolean(preferenceKey, false).also {
                        Timber.i("Updating $preferenceKey to $it")
                        syncedPrefUpdateReq.dataMap.putBoolean(preferenceKey, it)
                        BoolPreference(watchId, preferenceKey, it).also { boolPreference ->
                            database.boolPrefDao().update(boolPreference)
                        }
                    }
                }
                in SyncPreferences.INT_PREFS -> {
                    sharedPreferences.getInt(preferenceKey, 90).also {
                        Timber.i("Updating $preferenceKey to $it")
                        syncedPrefUpdateReq.dataMap.putInt(preferenceKey, it)
                        IntPreference(watchId, preferenceKey, it).also { intPreference ->
                            database.intPrefDao().update(intPreference)
                        }
                    }
                }
            }
            if (!syncedPrefUpdateReq.dataMap.isEmpty) {
                Timber.i("Sending updated preference")
                syncedPrefUpdateReq.setUrgent()
                return@withContext dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
            } else {
                Timber.w("No preference to update")
            }
            return@withContext null
        }
    }

    /**
     * Send all watch-specific preferences to a specified [Watch].
     * @param watchId The [Watch.id] to get preferences for and send preferences to.
     * @return The [Task] created by the preference send job, or null if the task failed.
     */
    internal suspend fun updateAllPreferencesOnWatch(watchId: String?): Task<DataItem>? {
        Timber.d("updateAllPreferencesOnWatch")
        if (!watchId.isNullOrEmpty()) {
            return withContext(Dispatchers.IO) {
                Timber.i("Creating update request")
                // Get updated preferences
                val intPrefs = database.intPrefDao().getAllForWatch(watchId)
                val boolPrefs = database.boolPrefDao().getAllForWatch(watchId)
                // Create PutDataMapRequest to send the new preferences
                val request =
                    PutDataMapRequest.create("/preference-change_$watchId").also { request ->
                        request.dataMap.apply {
                            intPrefs.forEach { putInt(it.key, it.value) }
                            boolPrefs.forEach { putBoolean(it.key, it.value) }
                        }
                        // Send updated preferences
                        request.setUrgent()
                    }
                return@withContext dataClient.putDataItem(request.asPutDataRequest())
            }
        } else {
            Timber.w("watchId null or empty")
        }
        return null
    }

    companion object {
        private var INSTANCE: WatchPreferenceManager? = null

        /** Get an instance of [WatchPreferenceManager] */
        fun get(context: Context): WatchPreferenceManager {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                if (INSTANCE == null) INSTANCE = WatchPreferenceManager(context)
                return INSTANCE!!
            }
        }
    }
}
