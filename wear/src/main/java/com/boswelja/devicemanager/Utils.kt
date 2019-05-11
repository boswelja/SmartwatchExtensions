/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import com.boswelja.devicemanager.common.References
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable

object Utils {

    fun getCompanionNode(context: Context): Task<CapabilityInfo> =
            Wearable.getCapabilityClient(context)
                    .getCapability(References.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)

    fun checkDnDAccess(context: Context): Boolean =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .isNotificationPolicyAccessGranted

    fun isTheaterModeOn(context: Context): Boolean =
            Settings.Global.getInt(context.contentResolver, "theater_mode_on", 0) == 1

    fun launchMobileApp(context: Context, key: String) {
        getCompanionNode(context).addOnSuccessListener { capabilityInfo ->
            val nodeId = capabilityInfo.nodes.firstOrNull { it.isNearby }?.id ?: capabilityInfo.nodes.firstOrNull()?.id!!
            Wearable.getMessageClient(context)
                    .sendMessage(nodeId, References.REQUEST_LAUNCH_APP_PATH, key.toByteArray(Charsets.UTF_8))
        }
    }
}
