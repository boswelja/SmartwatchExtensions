package com.boswelja.devicemanager.watchmanager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.BoolPreference
import com.boswelja.devicemanager.watchmanager.item.IntPreference
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class WatchPreferenceManager(context: Context) {

  private val dataClient = Wearable.getDataClient(context)
  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val database = WatchDatabase.get(context)

  /**
   * Sets all local [SharedPreferences] to their values stored with the connected [Watch], or their
   * default preference value.
   */
  fun updateLocalPreferences(watchId: String) {
    Timber.d("updateLocalPreferences() called")
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
    Timber.d("deleteLocalPreferences($commitNow) called")
    sharedPreferences.edit(commit = commitNow) {
      remove(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
      remove(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
      remove(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY)
      remove(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY)
      remove(PreferenceKey.DND_SYNC_TO_PHONE_KEY)
      remove(PreferenceKey.DND_SYNC_TO_WATCH_KEY)
      remove(PreferenceKey.DND_SYNC_WITH_THEATER_KEY)
      remove(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
    }
  }

  /**
   * Clear all preferences for a given [Watch].
   * @param watchId The ID of the [Watch] to clear preferences for.
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
   * Sends an updated preference to the connected [Watch].
   * @param preferenceKey The key of the preference to update on the watch.
   * @return The [Task] for the preference send job, or null if the task failed.
   */
  suspend fun updatePreferenceOnWatch(watchId: String, preferenceKey: String): Task<DataItem>? {
    Timber.d("updatePreferenceOnWatch($preferenceKey) called")
    return withContext(Dispatchers.IO) {
      val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_${watchId}")
      when (preferenceKey) {
        PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
        PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
        PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
        PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
        PreferenceKey.DND_SYNC_TO_PHONE_KEY,
        PreferenceKey.DND_SYNC_TO_WATCH_KEY,
        PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
          sharedPreferences.getBoolean(preferenceKey, false).also {
            Timber.i("Updating $preferenceKey to $it")
            syncedPrefUpdateReq.dataMap.putBoolean(preferenceKey, it)
            BoolPreference(watchId, preferenceKey, it).also { boolPreference ->
              database.boolPrefDao().update(boolPreference)
            }
          }
        }
        PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
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
   * Send all watch-specific preferences to the watch with the given ID.
   * @param watchId The ID of the [Watch] to get preferences from and send preferences to.
   * @return The [Task] created by the preference send job, or null if the task failed.
   */
  private suspend fun updateAllPreferencesOnWatch(watchId: String?): Task<DataItem>? {
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

}