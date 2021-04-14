package com.boswelja.smartwatchextensions.common.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * A data class containing information related to a device's battery.
 * @param percent The device's battery percent.
 * @param isCharging true if the device is charging, false otherwise.
 * @param lastUpdatedMillis The time in milliseconds this data was fetched.
 */
data class BatteryStats internal constructor(
    val percent: Int,
    val isCharging: Boolean,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {

    /**
     * Convert this [BatteryStats] to a [ByteArray].
     */
    fun toByteArray(): ByteArray {
        return "$percent|$isCharging".toByteArray(Charsets.UTF_8)
    }

    companion object {
        /**
         * Get a [BatteryStats] from a [ByteArray].
         */
        fun fromByteArray(byteArray: ByteArray): BatteryStats {
            val message = String(byteArray, Charsets.UTF_8)
            val messageSplit = message.split("|")
            val batteryPercent = messageSplit[0].toInt()
            val isWatchCharging = messageSplit[1] == true.toString()
            return BatteryStats(batteryPercent, isWatchCharging)
        }

        /**
         * Get an up to date [BatteryStats] for this device.
         * @param context [Context].
         * @return The [BatteryStats] for this device, or null if there was an issue.
         */
        fun createForDevice(context: Context): BatteryStats? {
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(null, iFilter)?.let {
                val batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val batteryScale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percent = (batteryLevel * 100) / batteryScale
                val charging = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                    BatteryManager.BATTERY_STATUS_CHARGING
                return BatteryStats(percent, charging)
            }
            return null
        }
    }
}
