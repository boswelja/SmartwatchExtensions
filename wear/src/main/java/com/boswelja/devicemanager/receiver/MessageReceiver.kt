/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.CommonUtils.boolToByteArray
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class MessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            References.REQUEST_WATCH_DND_ACCESS_STATUS_PATH -> {
                val hasDnDAccess: Boolean = Utils.checkDnDAccess(this)
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId,
                                References.REQUEST_WATCH_DND_ACCESS_STATUS_PATH,
                                boolToByteArray(hasDnDAccess))
            }
        }
    }
}