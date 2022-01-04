package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_STATS_NOTIFICATION_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first

class MobileBatterySyncNotificationHandler(
    private val settingsRepository: WatchSettingsRepository,
    private val watchRepository: WatchRepository,
    context: Context,
    notificationManager: NotificationManager
) : AndroidBatterySyncNotificationHandler(
    context, notificationManager
) {
    override suspend fun getDeviceName(targetUid: String): String {
        return watchRepository.getWatchById(targetUid).first()?.name ?: targetUid
    }

    override suspend fun onNotificationPosted(targetUid: String) {
        settingsRepository.putBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, true)
    }

    override suspend fun onNotificationCancelled(targetUid: String) {
        settingsRepository.putBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, false)
    }

    override suspend fun getNotificationAlreadySent(targetUid: String): Boolean {
        return settingsRepository.getBoolean(targetUid, BATTERY_STATS_NOTIFICATION_SENT, false).first()
    }

    override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean {
        return settingsRepository.getBoolean(targetUid, BATTERY_WATCH_CHARGE_NOTI_KEY, false).first()
    }

    override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean {
        return settingsRepository.getBoolean(targetUid, BATTERY_WATCH_LOW_NOTI_KEY, false).first()
    }

    override suspend fun getChargeThreshold(targetUid: String): Int {
        return settingsRepository.getInt(targetUid, BATTERY_CHARGE_THRESHOLD_KEY, 90).first()
    }

    override suspend fun getLowThreshold(targetUid: String): Int {
        return settingsRepository.getInt(targetUid, BATTERY_LOW_THRESHOLD_KEY, 20).first()
    }
}
