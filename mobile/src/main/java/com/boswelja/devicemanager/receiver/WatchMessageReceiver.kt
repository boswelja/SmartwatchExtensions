/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.app.NotificationManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            References.LOCK_PHONE_PATH -> {
                val devicePolicyManager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                if (devicePolicyManager.isAdminActive(DeviceAdminReceiver().getWho(this))) {
                    devicePolicyManager.lockNow()
                }
            }
            References.REQUEST_BATTERY_UPDATE_PATH ->
                CommonUtils.updateBatteryStats(this, References.CAPABILITY_WATCH_APP)
            References.REQUEST_LAUNCH_APP_PATH -> {
                val key = String(messageEvent.data, Charsets.UTF_8)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PREFERENCE_KEY, key)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            References.REQUEST_PHONE_DND_ACCESS_STATUS_PATH -> {
                val hasDnDAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notiManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId!!,
                                References.REQUEST_PHONE_DND_ACCESS_STATUS_PATH,
                                hasDnDAccess.toByteArray())
            }
        }
    }
}
