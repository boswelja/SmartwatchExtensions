package com.boswelja.smartwatchextensions.batterysync.platform

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_STATS_NOTIFICATION_SENT
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchLowNotificationEnabled
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first

/**
 * A [BatterySyncNotificationHandler] specifically for Mobile.
 */
class MobileBatterySyncNotificationHandler(
    private val settingsRepository: WatchSettingsRepository,
    private val registeredWatchRepository: RegisteredWatchRepository,
    private val getWatchLowNotificationEnabled: GetWatchLowNotificationEnabled,
    private val getWatchChargeNotificationEnabled: GetWatchChargeNotificationEnabled,
    private val getBatteryChargeThreshold: GetBatteryChargeThreshold,
    private val getBatteryLowThreshold: GetBatteryLowThreshold,
    context: Context,
    notificationManager: NotificationManager
) : BatterySyncNotificationHandler(context, notificationManager) {

    override suspend fun onNotificationPosted(targetUid: String) {
        settingsRepository.putBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, true)
    }

    override suspend fun onNotificationCancelled(targetUid: String) {
        settingsRepository.putBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, false)
    }

    override suspend fun getDeviceName(targetUid: String): String =
        registeredWatchRepository.getWatchById(targetUid).first()?.name ?: targetUid

    override suspend fun getNotificationAlreadySent(targetUid: String): Boolean =
        settingsRepository.getBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, false).first()

    override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean =
        getWatchChargeNotificationEnabled().first().getOrDefault(DefaultValues.NOTIFICATIONS_ENABLED)

    override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean =
        getWatchLowNotificationEnabled().first().getOrDefault(DefaultValues.NOTIFICATIONS_ENABLED)

    override suspend fun getChargeThreshold(targetUid: String): Int =
        getBatteryChargeThreshold().first().getOrDefault(DefaultValues.CHARGE_THRESHOLD)

    override suspend fun getLowThreshold(targetUid: String): Int =
        getBatteryLowThreshold().first().getOrDefault(DefaultValues.LOW_THRESHOLD)
}
