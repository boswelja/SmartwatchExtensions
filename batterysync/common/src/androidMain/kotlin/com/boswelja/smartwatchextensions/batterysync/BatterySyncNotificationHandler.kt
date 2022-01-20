package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.boswelja.smartwatchextensions.batterysync.common.R

/**
 * The base class for handling Battery Sync notifications.
 * @param context [Context].
 * @param notificationManager [NotificationManager].
 */
actual abstract class BatterySyncNotificationHandler(
    private val context: Context,
    private val notificationManager: NotificationManager
) : BaseBatterySyncNotificationHandler() {

    /**
     * Called when a notification is posted.
     * @param targetUid The UID of the device the notification belongs to.
     */
    actual abstract suspend fun onNotificationPosted(targetUid: String)

    /**
     * Called when a notification is cancelled.
     * @param targetUid The UID of the device the notification belonged to.
     */
    actual abstract suspend fun onNotificationCancelled(targetUid: String)

    /**
     * Get the name of the device with the given UID.
     * @param targetUid The device UID whose name to load.
     * @return The name of the device.
     */
    actual abstract suspend fun getDeviceName(targetUid: String): String

    actual override suspend fun cancelNotificationFor(targetUid: String) {
        notificationManager.cancel(calculateNotificationId(targetUid))
        onNotificationCancelled(targetUid)
    }

    actual override suspend fun postChargeNotificationFor(
        targetUid: String,
        batteryStats: BatteryStats
    ) {
        createNotificationChannel()
        val deviceName = getDeviceName(targetUid)
        val notification = createBaseNotificationBuilder()
            .setSmallIcon(R.drawable.battery_full)
            .setContentTitle(
                context.getString(R.string.charged_notification_title, deviceName)
            )
            .setContentText(
                context.getString(
                    R.string.charged_notification_desc,
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

    actual override suspend fun postLowNotificationFor(
        targetUid: String,
        batteryStats: BatteryStats
    ) {
        createNotificationChannel()
        val deviceName = getDeviceName(targetUid)
        val notification = createBaseNotificationBuilder()
            .setSmallIcon(R.drawable.battery_alert)
            .setContentTitle(
                context.getString(R.string.low_notification_title, deviceName)
            )
            .setContentText(
                context.getString(
                    R.string.low_notification_desc,
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

    /**
     * Create a [PendingIntent] to launch the app.
     */
    private fun createLaunchPendingIntent(): PendingIntent? {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            PendingIntent.getActivity(
                context,
                START_ACTIVITY_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } catch (ignored: Exception) {
            null
        }

    }

    private fun createBaseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentIntent(createLaunchPendingIntent())
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.battery_notification_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun calculateNotificationId(targetUid: String): Int = "$targetUid-batterysync".hashCode()

    companion object {
        internal const val NOTIFICATION_CHANNEL_ID = "companion_device_charged"
        private const val START_ACTIVITY_REQUEST_CODE = 123
    }

}
