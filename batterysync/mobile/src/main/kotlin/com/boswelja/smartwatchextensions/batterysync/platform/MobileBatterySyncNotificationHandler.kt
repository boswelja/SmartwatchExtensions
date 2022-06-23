package com.boswelja.smartwatchextensions.batterysync.platform

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_STATS_NOTIFICATION_SENT
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first

/**
 * A [BatterySyncNotificationHandler] specifically for Mobile.
 */
class MobileBatterySyncNotificationHandler(
    private val settingsRepository: WatchSettingsRepository,
    private val watchRepository: WatchRepository,
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
        watchRepository.getWatchById(targetUid).first()?.name ?: targetUid

    override suspend fun getNotificationAlreadySent(targetUid: String): Boolean =
        settingsRepository.getBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, false).first()

    override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean =
        settingsRepository
            .getBoolean(targetUid, BATTERY_WATCH_CHARGE_NOTI_KEY, DefaultValues.NOTIFICATIONS_ENABLED)
            .first()

    override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean =
        settingsRepository
            .getBoolean(targetUid, BATTERY_WATCH_LOW_NOTI_KEY, DefaultValues.NOTIFICATIONS_ENABLED)
            .first()

    override suspend fun getChargeThreshold(targetUid: String): Int =
        settingsRepository.getInt(targetUid, BATTERY_CHARGE_THRESHOLD_KEY, DefaultValues.CHARGE_THRESHOLD).first()

    override suspend fun getLowThreshold(targetUid: String): Int =
        settingsRepository.getInt(targetUid, BATTERY_LOW_THRESHOLD_KEY, DefaultValues.LOW_THRESHOLD).first()
}
