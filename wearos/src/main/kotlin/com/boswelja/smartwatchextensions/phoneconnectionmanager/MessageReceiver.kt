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
import com.boswelja.smartwatchextensions.messageClient
import com.boswelja.watchconnection.common.message.ByteArrayMessage
import com.boswelja.watchconnection.common.message.serialized.TypedMessage
import com.google.android.gms.wearable.MessageEvent
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
                runBlocking {
                    messageClient(listOf()).sendMessage(
                        ByteArrayMessage(
                            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                            hasDnDAccess.toByteArray()
                        )
                    )
                }
            }
            REQUEST_APP_VERSION -> {
                runBlocking {
                    val version = Version(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
                    messageClient(listOf(VersionSerializer)).sendMessage(
                        TypedMessage(REQUEST_APP_VERSION, version)
                    )
                }
            }
            REQUEST_SDK_INT_PATH -> {
                runBlocking {
                    messageClient(listOf()).sendMessage(
                        ByteArrayMessage(
                            REQUEST_SDK_INT_PATH,
                            Build.VERSION.SDK_INT.toBigInteger().toByteArray()
                        )
                    )
                }
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
