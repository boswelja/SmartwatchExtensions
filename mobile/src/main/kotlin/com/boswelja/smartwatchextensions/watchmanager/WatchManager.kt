package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.analytics.getAnalytics
import com.boswelja.smartwatchextensions.appmanager.AppCacheUpdateWorker
import com.boswelja.smartwatchextensions.appmanager.BaseAppCacheUpdateWorker
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepositoryLoader
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.RESET_APP
import com.boswelja.smartwatchextensions.dndsync.DnDStatusSerializer
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.IntSetting
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.settings.RESET_SETTINGS
import com.boswelja.smartwatchextensions.settings.UPDATE_BOOL_PREFERENCE
import com.boswelja.smartwatchextensions.settings.UPDATE_INT_PREFERENCE
import com.boswelja.smartwatchextensions.settings.WatchSettingsDbRepository
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabaseLoader
import com.boswelja.smartwatchextensions.watchmanager.database.RegisteredWatchDatabaseLoader
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageSerializer
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform
import com.boswelja.watchconnection.wearos.message.WearOSMessagePlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * Provides a simplified interface for interacting with all watch-related classes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WatchManager internal constructor(
    private val context: Context,
    val settingsRepository: WatchSettingsRepository,
    private val watchRepository: WatchRepository,
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient,
    private val analytics: Analytics,
    private val selectedWatchManager: SelectedWatchManager =
        SelectedWatchStoreManager(context.selectedWatchStateStore, watchRepository)
) {

    constructor(context: Context) : this(
        context.applicationContext,
        WatchSettingsDbRepository(WatchSettingsDatabaseLoader(context).createDatabase()),
        WatchDbRepository(
            DiscoveryClient(
                listOf(
                    WearOSDiscoveryPlatform(context)
                )
            ),
            RegisteredWatchDatabaseLoader(context).createDatabase()
        ),
        MessageClient(
            serializers = listOf(
                IntSettingSerializer,
                BoolSettingSerializer,
                DnDStatusSerializer,
                CacheValidationSerializer,
                BatteryStatsSerializer
            ),
            platforms = listOf(
                WearOSMessagePlatform(context)
            )
        ),
        DiscoveryClient(
            listOf(
                WearOSDiscoveryPlatform(context)
            )
        ),
        getAnalytics()
    )

    val registeredWatches: Flow<List<Watch>> = watchRepository.registeredWatches

    val availableWatches: Flow<List<Watch>> = watchRepository.availableWatches

    /**
     * The currently selected watch
     */
    val selectedWatch: Flow<Watch?> = selectedWatchManager.selectedWatch

    suspend fun registerWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            messageClient.sendMessage(watch, Message(Messages.WATCH_REGISTERED_PATH, null))
            watchRepository.registerWatch(watch)
            BaseAppCacheUpdateWorker.enqueueWorkerFor<AppCacheUpdateWorker>(context, watch.uid)
            analytics.logWatchRegistered()
        }
    }

    suspend fun forgetWatch(context: Context, watch: Watch) {
        forgetWatch(
            context.widgetIdStore,
            BatteryStatsRepositoryLoader.getInstance(context),
            watch
        )
    }

    internal suspend fun forgetWatch(
        widgetIdStore: DataStore<Preferences>,
        batteryStatsDatabase: BatteryStatsRepository,
        watch: Watch
    ) {
        withContext(Dispatchers.IO) {
            batteryStatsDatabase.removeStatsFor(watch.uid)
            messageClient.sendMessage(watch, Message(RESET_APP, null))
            watchRepository.deregisterWatch(watch)
            removeWidgetsForWatch(watch, widgetIdStore)
            BaseAppCacheUpdateWorker.stopWorkerFor(context, watch.uid)
            analytics.logWatchRemoved()
        }
    }

    suspend fun renameWatch(watch: Watch, newName: String) {
        withContext(Dispatchers.IO) {
            watchRepository.renameWatch(watch, newName)
            analytics.logWatchRenamed()
        }
    }

    suspend fun resetWatchPreferences(context: Context, watch: Watch) {
        resetWatchPreferences(
            context.widgetIdStore,
            BatteryStatsRepositoryLoader.getInstance(context),
            watch
        )
    }

    internal suspend fun resetWatchPreferences(
        widgetIdStore: DataStore<Preferences>,
        batteryStatsDatabase: BatteryStatsRepository,
        watch: Watch
    ) {
        batteryStatsDatabase.removeStatsFor(watch.uid)
        messageClient.sendMessage(watch, Message(RESET_SETTINGS, null))
        settingsRepository.deleteForWatch(watch.uid)
        removeWidgetsForWatch(watch, widgetIdStore)
    }

    internal suspend fun removeWidgetsForWatch(
        watch: Watch,
        widgetIdStore: DataStore<Preferences>
    ) {
        val widgetsToRemove = widgetIdStore.data.firstOrNull()?.asMap()
            ?.filter { it.value == watch.uid }?.keys
        if (!widgetsToRemove.isNullOrEmpty()) {
            widgetIdStore.edit { mutablePreferences ->
                widgetsToRemove.forEach {
                    mutablePreferences.remove(it)
                }
            }
        }
    }

    /**
     * Selects a watch by a given [Watch.uid]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    suspend fun selectWatchById(watchId: String) = selectedWatchManager.selectWatch(watchId)

    fun getStatusFor(watch: Watch) = discoveryClient.connectionModeFor(watch)

    suspend fun getCapabilitiesFor(watch: Watch) =
        discoveryClient.getCapabilitiesFor(watch).map { capability ->
            Capability.valueOf(capability)
        }

    fun selectedWatchHasCapability(capability: Capability): Flow<Boolean> =
        selectedWatch.flatMapLatest { watch ->
            watch?.let {
                watchRepository.watchHasCapability(watch, capability)
            } ?: flowOf(false)
        }

    suspend fun sendMessage(
        watch: Watch,
        message: String,
        data: ByteArray? = null,
        priority: Message.Priority = Message.Priority.LOW
    ) = messageClient.sendMessage(watch, Message(message, data, priority))

    suspend fun sendMessage(
        watch: Watch,
        message: Message<*>
    ) = messageClient.sendMessage(watch, message)

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

    fun getWatchById(id: String): Flow<Watch?> = watchRepository.getWatchById(id)

    fun incomingMessages() = messageClient.rawIncomingMessages()
    fun <T> incomingMessages(serializer: MessageSerializer<T>) =
        messageClient.incomingMessages(serializer)

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager)
}
