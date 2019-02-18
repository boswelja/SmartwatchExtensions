package com.boswelja.devicemanager.service

import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.CommonUtils.boolToByteArray
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class MessageListener : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            References.REQUEST_DND_ACCESS_STATUS -> {
                val hasDnDAccess: Boolean = Utils.checkDnDAccess(this)
                Wearable.getMessageClient(this)
                        .sendMessage(messageEvent.sourceNodeId, References.REQUEST_DND_ACCESS_STATUS, boolToByteArray(hasDnDAccess))
            }
        }
    }
}