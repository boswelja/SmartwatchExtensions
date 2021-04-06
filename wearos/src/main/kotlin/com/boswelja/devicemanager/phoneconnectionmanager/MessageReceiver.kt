package com.boswelja.devicemanager.phoneconnectionmanager

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.appmanager.AppManagerService
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.appmanager.Messages
import com.boswelja.devicemanager.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.devicemanager.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.devicemanager.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.devicemanager.common.connection.Messages.RESET_APP
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_SDK_INT_PATH
import com.boswelja.devicemanager.common.toByteArray
import com.boswelja.devicemanager.extensions.SettingsSerializer
import com.boswelja.devicemanager.extensions.extensionSettingsStore
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Timber.d("Received ${messageEvent?.path}")
        when (messageEvent?.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasDnDAccess: Boolean = Compat.canSetDnD(this)
                Wearable.getMessageClient(this)
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                        hasDnDAccess.toByteArray()
                    )
            }
            REQUEST_APP_VERSION -> {
                Wearable.getMessageClient(this)
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        REQUEST_APP_VERSION,
                        (BuildConfig.VERSION_NAME + "|" + BuildConfig.VERSION_CODE).toByteArray(
                            Charsets.UTF_8
                        )
                    )
            }
            Messages.START_SERVICE -> {
                val intent = Intent(this, AppManagerService::class.java)
                ContextCompat.startForegroundService(this, intent)
            }
            REQUEST_SDK_INT_PATH -> {
                Wearable.getMessageClient(this)
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        REQUEST_SDK_INT_PATH,
                        Build.VERSION.SDK_INT.toBigInteger().toByteArray()
                    )
            }
            RESET_APP -> {
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.clearApplicationUserData()
            }
            CLEAR_PREFERENCES -> {
                runBlocking {
                    extensionSettingsStore.updateData {
                        // Recreate the DataStore with default values
                        SettingsSerializer().defaultValue
                    }
                }
            }
            REQUEST_UPDATE_CAPABILITIES -> {
                CapabilityUpdater(this).updateCapabilities()
            }
        }
    }
}
