/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.preference.PreferenceManager
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import java.util.UUID

object CommonUtils {

    fun updateBatteryStats(context: Context) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val batteryPct = ((batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! / batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()) * 100).toInt()
        val charging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        val message = "$batteryPct|$charging"

        Wearable.getCapabilityClient(context)
                .getCapability(References.CAPABILITY_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener { capabilityInfo ->
                    val nodeId = capabilityInfo.nodes.firstOrNull { it.isNearby }?.id ?: capabilityInfo.nodes.firstOrNull()?.id
                    if (nodeId != null) {
                        val messageClient = Wearable.getMessageClient(context)
                        messageClient.sendMessage(
                                nodeId,
                                References.BATTERY_STATUS_PATH,
                                message.toByteArray(Charsets.UTF_8))
                    }
                }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun boolToByteArray(b: Boolean): ByteArray {
        val byte: Byte = if (b) {
            1
        } else {
            0
        }
        return byteArrayOf(byte)
    }

    fun getUUID(context: Context) : String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        var uuid = prefs.getString(PreferenceKey.DEVICE_UUID_KEY, "")
        if (uuid.isNullOrBlank()) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(PreferenceKey.DEVICE_UUID_KEY, uuid).apply()
        }
        return uuid
    }
}