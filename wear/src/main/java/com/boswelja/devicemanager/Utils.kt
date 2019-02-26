/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object Utils {

    fun isCompanionAppInstalled(context: Context, capabilityCallbacks: CapabilityCallbacks) {
        Wearable
                .getCapabilityClient(context)
                .getCapability(
                        References.CAPABILITY_APP,
                        CapabilityClient.FILTER_REACHABLE
                )
                .addOnSuccessListener {
                    val node = it.nodes.lastOrNull()
                    if (node != null) {
                        capabilityCallbacks.capableDeviceFound(node)
                    } else {
                        capabilityCallbacks.noCapableDevices()
                    }
                }
    }

    interface CapabilityCallbacks {
        fun capableDeviceFound(node: Node?)

        fun noCapableDevices()
    }

    fun checkDnDAccess(context: Context): Boolean {
        val notiManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasDnDAccess = notiManager.isNotificationPolicyAccessGranted
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (hasDnDAccess != prefs.getBoolean(PreferenceKey.DND_ACCESS_STATUS_KEY, false)) {
            prefs.edit().putBoolean(PreferenceKey.DND_ACCESS_STATUS_KEY, hasDnDAccess).apply()
        }
        Log.d("Utils", hasDnDAccess.toString())
        return hasDnDAccess
    }
}