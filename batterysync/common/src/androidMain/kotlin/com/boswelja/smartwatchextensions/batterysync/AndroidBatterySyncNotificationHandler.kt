package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.boswelja.smartwatchextensions.batterysync.common.R

abstract class AndroidBatterySyncNotificationHandler(
    private val context: Context,
    private val notificationManager: NotificationManager
) : BatterySyncNotificationHandler() {

    abstract suspend fun onNotificationPosted(targetUid: String)

    abstract suspend fun onNotificationCancelled(targetUid: String)

    abstract suspend fun getDeviceName(targetUid: String): String

    final override suspend fun postChargeNotificationFor(targetUid: String, batteryStats: BatteryStats) {
        val deviceName = getDeviceName(targetUid)
        val notification = createBaseNotificationBuilder()
            .setSmallIcon(R.drawable.battery_full)
            .setContentTitle(
                context.getString(R.string.device_battery_charged_noti_title, deviceName)
            )
            .setContentText(
                context.getString(
                    R.string.device_battery_charged_noti_desc,
                    deviceName,
                    batteryStats.percent.toString()
                )
            )
            .build()

        notificationManager.notify(
            calculateNotificationId(targetUid),
            notification
        )
        onNotificationPosted(targetUid)
    }

    final override suspend fun postLowNotificationFor(targetUid: String, batteryStats: BatteryStats) {
        val deviceName = getDeviceName(targetUid)
        val notification = createBaseNotificationBuilder()
            .setSmallIcon(R.drawable.battery_alert)
            .setContentTitle(
                context.getString(R.string.device_battery_low_noti_title, deviceName)
            )
            .setContentText(
                context.getString(
                    R.string.device_battery_low_noti_desc,
                    deviceName,
                    batteryStats.percent.toString()
                )
            )
            .build()

        notificationManager.notify(
            calculateNotificationId(targetUid),
            notification
        )
        onNotificationPosted(targetUid)
    }

    final override suspend fun cancelNotificationFor(targetUid: String) {
        notificationManager.cancel(calculateNotificationId(targetUid))
        onNotificationCancelled(targetUid)
    }

    /**
     * Create a [PendingIntent] to launch the app.
     */
    private fun createLaunchPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            START_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createBaseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
            .setContentIntent(createLaunchPendingIntent())
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
    }

    private fun calculateNotificationId(targetUid: String): Int = "$targetUid-batterysync".hashCode()

    companion object {
        private const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"
        private const val START_ACTIVITY_REQUEST_CODE = 123
    }
}
