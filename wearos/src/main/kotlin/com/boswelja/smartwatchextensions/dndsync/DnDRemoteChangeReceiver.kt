package com.boswelja.smartwatchextensions.dndsync

import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path != DND_STATUS_PATH) return
        val interruptFilterEnabled = Boolean.fromByteArray(messageEvent.data)
        Compat.setInterruptionFilter(this, interruptFilterEnabled)
        // TODO Let phone know if there's as an issue
    }
}
