package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Provides a simplified interface for interacting with all watch-related classes.
 */
@Suppress("LongParameterList", "TooManyFunctions", "UndocumentedPublicProperty")
class WatchManager(
    private val messageClient: MessageClient,
    private val watchRepository: WatchRepository,
    private val selectedWatchManager: SelectedWatchManager,
    val settingsRepository: WatchSettingsRepository
) {

    /**
     * Flow a list of registered watches.
     */
    val registeredWatches: Flow<List<Watch>> = watchRepository.registeredWatches

    /**
     * The currently selected watch
     */
    val selectedWatch: Flow<Watch?> = selectedWatchManager.selectedWatch

    /**
     * Selects a watch by a given [Watch.uid]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    suspend fun selectWatchById(watchId: String) = selectedWatchManager.selectWatch(watchId)

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
    ) = messageClient.sendMessage(watch.uid, Message(message, data, priority))

    /**
     * Update a preference for a given watch.
     * @param watch The [Watch] to send the preference update to.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    @Deprecated("Use an appropriate MessageHandler instead")
    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        withContext(Dispatchers.IO) {
            when (value) {
                is Boolean -> {
                    val handler = MessageHandler(BoolSettingSerializer, messageClient)
                    handler.sendMessage(
                        watch.uid,
                        Message(
                            UpdateBoolSetting,
                            BoolSetting(key, value)
                        )
                    )
                    settingsRepository.putBoolean(watch.uid, key, value)
                }
                is Int -> {
                    val handler = MessageHandler(IntSettingSerializer, messageClient)
                    handler.sendMessage(
                        watch.uid,
                        Message(
                            UpdateIntSetting,
                            IntSetting(key, value)
                        )
                    )
                    settingsRepository.putInt(watch.uid, key, value)
                }
            }
        }
    }

    /**
     * Flow a watch with the given ID.
     * @param id The ID for the watch to flow.
     */
    fun getWatchById(id: String): Flow<Watch?> = watchRepository.getWatchById(id)
}
