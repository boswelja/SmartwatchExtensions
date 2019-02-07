/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearMessageReceiverService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        val messagePath = messageEvent?.path
        when (messagePath) {
            References.LOCK_PHONE_KEY -> {
                val devicePolicyManager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                if (devicePolicyManager.isAdminActive(DeviceAdminReceiver().getWho(this))) {
                    devicePolicyManager.lockNow()
                }
            }
            References.REQUEST_BATTERY_UPDATE_KEY -> {
                CommonUtils.updateBatteryStats(this)
            }
        }
    }
}