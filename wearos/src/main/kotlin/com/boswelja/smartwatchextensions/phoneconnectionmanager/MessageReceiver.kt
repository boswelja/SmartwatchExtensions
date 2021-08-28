package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.smartwatchextensions.common.connection.Messages.RESET_APP
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_SDK_INT_PATH
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.common.versioning.Version
import com.boswelja.smartwatchextensions.common.versioning.VersionSerializer
import com.boswelja.smartwatchextensions.extensions.SettingsSerializer
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
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
                val hasDnDAccess =
                    getSystemService<NotificationManager>()!!.isNotificationPolicyAccessGranted
                Wearable.getMessageClient(this)
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                        hasDnDAccess.toByteArray()
                    )
            }
            REQUEST_APP_VERSION -> {
                val data = runBlocking {
                    VersionSerializer.serialize(
                        Version(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
                    )
                }
                Wearable.getMessageClient(this)
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        REQUEST_APP_VERSION,
                        data
                    )
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
                runBlocking {
                    CapabilityUpdater(this@MessageReceiver).updateCapabilities()
                }
            }
        }
    }
}
