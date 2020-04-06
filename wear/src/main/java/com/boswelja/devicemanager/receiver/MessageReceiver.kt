/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Intent
import android.os.Build
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.appmanager.AppManagerService
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.References.REQUEST_APP_VERSION
import com.boswelja.devicemanager.common.appmanager.References
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_SDK_INT_PATH
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class MessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasDnDAccess: Boolean = Utils.checkDnDAccess(this)
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId,
                                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                                hasDnDAccess.toByteArray())
            }
            REQUEST_APP_VERSION -> {
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId,
                                REQUEST_APP_VERSION,
                                (BuildConfig.VERSION_NAME + "|" + BuildConfig.VERSION_CODE).toByteArray(Charsets.UTF_8))
            }
            References.START_SERVICE -> {
                val intent = Intent(this, AppManagerService::class.java)
                Compat.startForegroundService(this, intent)
            }
            REQUEST_SDK_INT_PATH -> {
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId,
                                REQUEST_SDK_INT_PATH,
                                Build.VERSION.SDK_INT.toBigInteger().toByteArray())
            }
        }
    }
}
