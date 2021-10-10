package com.boswelja.smartwatchextensions.settings

import kotlinx.coroutines.flow.Flow

/**
 * A repository for storing and retrieving key/value pairs tied to a specific watch.
 */
interface WatchSettingsRepository {

    /**
     * Store a boolean value.
     * @param watchId The UID of the watch this setting is tied to.
     * @param key The setting key.
     * @param value The value to store.
     */
    suspend fun putBoolean(watchId: String, key: String, value: Boolean)

    /**
     * Store a integer value.
     * @param watchId The UID of the watch this setting is tied to.
     * @param key The setting key.
     * @param value The value to store.
     */
    suspend fun putInt(watchId: String, key: String, value: Int)

    /**
     * Flow a boolean value, or [defaultValue] if there is no matching setting stored.
     * @param watchId The UID of the watch the setting belongs to.
     * @param key The setting key.
     * @param defaultValue The value to emit when no value is found.
     */
    fun getBoolean(watchId: String, key: String, defaultValue: Boolean = false): Flow<Boolean>

    /**
     * Flow a integer value, or [defaultValue] if there is no matching setting stored.
     * @param watchId The UID of the watch the setting belongs to.
     * @param key The setting key.
     * @param defaultValue The value to emit when no value is found.
     */
    fun getInt(watchId: String, key: String, defaultValue: Int = 0): Flow<Int>

    /**
     * Delete all settings for a watch with the given ID.
     * @param watchId The watch UID to delete settings for.
     */
    suspend fun deleteForWatch(watchId: String)
}
