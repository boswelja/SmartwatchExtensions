package com.boswelja.devicemanager.receiver

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.boswelja.devicemanager.common.Config
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearMessageReceiverService: WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        val devicePolicyManager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val messagePath = messageEvent?.path
        when (messagePath) {
            Config.LOCK_PHONE_PATH -> {
                if (devicePolicyManager.isAdminActive(DeviceAdminReceiver().getWho(this))) {
                    devicePolicyManager.lockNow()
                } else {
                    Utils.requestDeviceAdminPerms(this)
                }
            }
        }
    }
}