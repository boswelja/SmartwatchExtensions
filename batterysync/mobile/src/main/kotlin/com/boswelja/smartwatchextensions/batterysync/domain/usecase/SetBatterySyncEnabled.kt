package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.platform.BatterySyncWorker
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SetBatterySyncEnabled(
    private val context: Context,
    private val settingsRepository: WatchSettingsRepository,
    private val messageClient: MessageClient,
    private val selectedWatchController: SelectedWatchController
) {
    suspend operator fun invoke(watchId: String, batterySyncEnabled: Boolean): Boolean {
        // We can only enable sync if the sync worker was started successfully
        if (batterySyncEnabled && !BatterySyncWorker.startSyncingFor(context, watchId)) return false

        // Update the watches local value
        val updateSent = messageClient.sendMessage(
            targetId = watchId,
            path = UpdateBoolSetting,
            data = BoolSettingSerializer.serialize(BoolSetting(BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled))
        )

        return if (!updateSent) {
            false
        } else {
            // Set the new value
            settingsRepository.putBoolean(watchId, BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
            true
        }
    }

    suspend operator fun invoke(batterySyncEnabled: Boolean): Boolean {
        val watchId = selectedWatchController.selectedWatch
            .map { it?.uid }
            .first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, batterySyncEnabled)
    }
}
