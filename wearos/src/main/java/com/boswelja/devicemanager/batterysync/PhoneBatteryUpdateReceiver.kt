package com.boswelja.devicemanager.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.batterysync.BatteryStats
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class PhoneBatteryUpdateReceiver : WearableListenerService() {

    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            Timber.i("Got battery stats from ${messageEvent.sourceNodeId}")

            val batteryStats = BatteryStats.fromMessage(messageEvent)
            sharedPreferences.edit {
                putInt(PreferenceKey.BATTERY_PERCENT_KEY, batteryStats.percent)
            }

            handleChargeNotification(batteryStats)

            updateBatteryStats(this, messageEvent.sourceNodeId)
            PhoneBatteryComplicationProvider.updateAll(this)
        }
    }

    /**
     * Decides whether a device charged notification should be sent to the user, and either sends
     * the notification or cancels any existing notifications accordingly.
     * @param batteryStats The [BatteryStats] object to read data from.
     */
    private fun handleChargeNotification(batteryStats: BatteryStats) {
        Timber.d("handleChargeNotification($batteryStats) called")
        val shouldNotifyUser =
            sharedPreferences.getBoolean(BATTERY_PHONE_CHARGE_NOTI_KEY, false)
        if (batteryStats.isCharging && shouldNotifyUser) {
            val chargeThreshold =
                sharedPreferences.getInt(BATTERY_CHARGE_THRESHOLD_KEY, 90)
            val hasNotiBeenSent =
                sharedPreferences.getBoolean(BATTERY_CHARGED_NOTI_SENT, false)

            if (batteryStats.percent >= chargeThreshold && !hasNotiBeenSent) {
                val phoneName =
                    sharedPreferences.getString(
                        PHONE_NAME_KEY, getString(R.string.default_phone_name)
                    )
                        ?: getString(R.string.default_phone_name)
                notifyCharged(phoneName, chargeThreshold)
            }
        } else {
            Timber.i("Cancelling any existing charge notifications")
            getSystemService<NotificationManager>()?.cancel(BATTERY_CHARGED_NOTI_ID)
            sharedPreferences.edit { putBoolean(BATTERY_CHARGED_NOTI_SENT, false) }
        }
    }

    /**
     * Creates and sends the device charged [NotificationCompat]. This will also create the required
     * [NotificationChannel] if necessary.
     * @param deviceName The name of the device that's charged.
     * @param chargeThreshold The minimum charge percent required to send the device charged
     * notification.
     */
    private fun notifyCharged(deviceName: String, chargeThreshold: Int) {
        Timber.d("notifyCharged($deviceName, $chargeThreshold) called")
        getSystemService<NotificationManager>()?.let { notificationManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                notificationManager.getNotificationChannel(BATTERY_CHARGED_NOTI_CHANNEL_ID) ==
                null
            ) {
                Timber.i("Creating notification channel")
                val channel =
                    NotificationChannel(
                        BATTERY_CHARGED_NOTI_CHANNEL_ID,
                        getString(R.string.noti_channel_phone_charged_title),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                        .apply {
                            enableVibration(true)
                            setShowBadge(true)
                        }
                notificationManager.createNotificationChannel(channel)
            }

            val noti =
                NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(getString(R.string.device_charged_noti_title, deviceName))
                    .setContentText(
                        getString(
                            R.string.device_charged_noti_desc,
                            deviceName,
                            chargeThreshold.toString()
                        )
                    )
                    .setLocalOnly(true)
                    .build()

            notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
            sharedPreferences.edit { putBoolean(BATTERY_CHARGED_NOTI_SENT, true) }
            Timber.i("Notification sent")
        }
    }

    /**
     * Get battery stats for this device.
     * @param function The function to be called when we've got the battery stats. This may not be
     * called if there's an issue retrieving battery stats. The function is called with battery
     * percent and a boolean to represent whether the device is charging.
     */
    private fun Context.getBatteryStats(function: (Int, Boolean) -> Unit) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(null, iFilter)?.let {
            val batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val batteryScale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = (batteryLevel * 100) / batteryScale
            val charging = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                BatteryManager.BATTERY_STATUS_CHARGING
            function(percent, charging)
        }
    }

    /** Sends a battery status update to connected devices. */
    private fun updateBatteryStats(context: Context, phoneId: String) {
        context.getBatteryStats { percent, isCharging ->
            val message = "$percent|$isCharging".toByteArray(Charsets.UTF_8)
            Wearable.getMessageClient(context).sendMessage(phoneId, BATTERY_STATUS_PATH, message)
        }
    }

    companion object {
        private const val BATTERY_CHARGED_NOTI_ID = 408565
        private const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
    }
}
