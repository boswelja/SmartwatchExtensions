package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appmanager.AppCacheUpdateWorker
import com.boswelja.smartwatchextensions.appmanager.BaseAppCacheUpdateWorker
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.IntSetting
import com.boswelja.smartwatchextensions.settings.RESET_SETTINGS
import com.boswelja.smartwatchextensions.settings.UPDATE_BOOL_PREFERENCE
import com.boswelja.smartwatchextensions.settings.UPDATE_INT_PREFERENCE
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageSerializer
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * Provides a simplified interface for interacting with all watch-related classes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList", "TooManyFunctions", "UndocumentedPublicProperty")
class WatchManager(
    private val context: Context,
    private val analytics: Analytics,
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient,
    private val watchRepository: WatchRepository,
    private val selectedWatchManager: SelectedWatchManager,
    private val batteryStatsRepository: BatteryStatsRepository,
    val settingsRepository: WatchSettingsRepository
) {

    /**
     * Flow a list of registered watches.
     */
    val registeredWatches: Flow<List<Watch>> = watchRepository.registeredWatches

    /**
     * Flow a list of available watches.
     */
    val availableWatches: Flow<List<Watch>> = watchRepository.availableWatches

    /**
     * The currently selected watch
     */
    val selectedWatch: Flow<Watch?> = selectedWatchManager.selectedWatch

    /**
     * Register a watch.
     * @param watch The watch to register.
     */
    suspend fun registerWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            messageClient.sendMessage(watch, Message(WATCH_REGISTERED_PATH, null))
            watchRepository.registerWatch(watch)
            BaseAppCacheUpdateWorker.enqueueWorkerFor<AppCacheUpdateWorker>(context, watch.uid)
            analytics.logWatchRegistered()
        }
    }

    /**
     * Forget a watch.
     * @param watch The watch to forget.
     */
    suspend fun forgetWatch(
        watch: Watch
    ) {
        withContext(Dispatchers.IO) {
            batteryStatsRepository.removeStatsFor(watch.uid)
            messageClient.sendMessage(watch, Message(RESET_APP, null))
            watchRepository.deregisterWatch(watch)
            BaseAppCacheUpdateWorker.stopWorkerFor(context, watch.uid)
            analytics.logWatchRemoved()
        }
    }

    /**
     * Rename a watch.
     * @param watch The watch to rename.
     * @param newName The watches new name.
     */
    suspend fun renameWatch(watch: Watch, newName: String) {
        withContext(Dispatchers.IO) {
            watchRepository.renameWatch(watch, newName)
            analytics.logWatchRenamed()
        }
    }

    /**
     * Reset a watches preferences.
     * @param watch The watch whose preferences to reset.
     */
    suspend fun resetWatchPreferences(
        watch: Watch
    ) {
        batteryStatsRepository.removeStatsFor(watch.uid)
        messageClient.sendMessage(watch, Message(RESET_SETTINGS, null))
        settingsRepository.deleteForWatch(watch.uid)
    }

    /**
     * Selects a watch by a given [Watch.uid]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    suspend fun selectWatchById(watchId: String) = selectedWatchManager.selectWatch(watchId)

    /**
     * Flow the status for a given watch.
     * @param watch The watch whose status to collect.
     */
    fun getStatusFor(watch: Watch) = discoveryClient.connectionModeFor(watch)

    /**
     * Get all capabilities for a given watch.
     * @param watch The watch to get capabilities for.
     */
    suspend fun getCapabilitiesFor(watch: Watch) =
        discoveryClient.getCapabilitiesFor(watch).mapNotNull { capability ->
            try {
                Capability.valueOf(capability)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

    /**
     * Flow whether the selected watch has the given capability.
     * @param capability The capability to check for.
     */
    fun selectedWatchHasCapability(capability: Capability): Flow<Boolean> =
        selectedWatch.flatMapLatest { watch ->
            watch?.let {
                watchRepository.watchHasCapability(watch, capability)
            } ?: flowOf(false)
        }

    /**
     * Send a message to a target watch.
     * @param watch The target watch.
     * @param message The message path.
     * @param data The message data bytes, or null if the message has no data.
     * @param priority The message priority.
     */
    suspend fun sendMessage(
        watch: Watch,
        message: String,
        data: ByteArray? = null,
        priority: Message.Priority = Message.Priority.LOW
    ) = messageClient.sendMessage(watch, Message(message, data, priority))

    /**
     * Send a message to a target watch.
     * @param watch The target watch.
     * @param message The message to send.
     */
    suspend fun sendMessage(
        watch: Watch,
        message: Message<*>
    ) = messageClient.sendMessage(watch, message)

    /**
     * Retrieve a boolean setting.
     * @param key The setting key to fetch.
     * @param watch The watch to load the setting for. If this is null, the selected watch will be
     * used isntead.
     * @param default The default value.
     */
    fun getBoolSetting(key: String, watch: Watch? = null, default: Boolean = false): Flow<Boolean> {
        return if (watch != null) {
            settingsRepository.getBoolean(watch.uid, key, default)
        } else {
            selectedWatch.flatMapLatest { selectedWatch ->
                if (selectedWatch != null) {
                    settingsRepository.getBoolean(selectedWatch.uid, key, default)
                } else {
                    flowOf(default)
                }
            }
        }
    }

    /**
     * Retrieve an int setting.
     * @param key The setting key to fetch.
     * @param watch The watch to load the setting for. If this is null, the selected watch will be
     * used isntead.
     * @param default The default value.
     */
    fun getIntSetting(key: String, watch: Watch? = null, default: Int = 0): Flow<Int> {
        return if (watch != null) {
            settingsRepository.getInt(watch.uid, key, default)
        } else {
            selectedWatch.flatMapLatest { selectedWatch ->
                if (selectedWatch != null) {
                    settingsRepository.getInt(selectedWatch.uid, key, default)
                } else {
                    flowOf(default)
                }
            }
        }
    }

    /**
     * Update a preference for a given watch.
     * @param watch The [Watch] to send the preference update to.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        withContext(Dispatchers.IO) {
            when (value) {
                is Boolean -> {
                    messageClient.sendMessage(
                        watch,
                        Message(
                            UPDATE_BOOL_PREFERENCE,
                            BoolSetting(key, value)
                        )
                    )
                    settingsRepository.putBoolean(watch.uid, key, value)
                }
                is Int -> {
                    messageClient.sendMessage(
                        watch,
                        Message(
                            UPDATE_INT_PREFERENCE,
                            IntSetting(key, value)
                        )
                    )
                    settingsRepository.putInt(watch.uid, key, value)
                }
            }

            analytics.logExtensionSettingChanged(key, value)
        }
    }

    /**
     * Update a preference for all registered watches.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(key: String, value: Boolean) {
        registeredWatches.first().forEach {
            updatePreference(it, key, value)
        }
    }

    /**
     * Flow a watch with the given ID.
     * @param id The ID for the watch to flow.
     */
    fun getWatchById(id: String): Flow<Watch?> = watchRepository.getWatchById(id)

    /**
     * Flow incoming messages.
     */
    fun incomingMessages() = messageClient.rawIncomingMessages()

    /**
     * Flow incoming messages that can be deserialized by the given serializer.
     * @param serializer The serializer to use.
     */
    fun <T> incomingMessages(serializer: MessageSerializer<T>) =
        messageClient.incomingMessages(serializer)
}
