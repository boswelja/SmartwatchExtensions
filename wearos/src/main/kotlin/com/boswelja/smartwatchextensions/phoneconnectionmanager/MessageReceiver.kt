package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.core.devicemanagement.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.core.devicemanagement.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.smartwatchextensions.core.devicemanagement.RESET_APP
import com.boswelja.smartwatchextensions.core.devicemanagement.Version
import com.boswelja.smartwatchextensions.core.devicemanagement.VersionSerializer
import com.boswelja.smartwatchextensions.core.settings.RESET_SETTINGS
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving empty messages.
 */
class MessageReceiver :
    MessageReceiver(),
    KoinComponent {

    private val messageClient: MessageClient by inject()
    private val discoveryClient: DiscoveryClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        when (message.path) {
            REQUEST_APP_VERSION -> {
                val handler = MessageHandler(VersionSerializer, messageClient)
                val version = Version(BuildConfig.VERSION_CODE.toLong(), BuildConfig.VERSION_NAME)
                handler.sendMessage(
                    discoveryClient.pairedPhone()!!.uid,
                    Message(REQUEST_APP_VERSION, version)
                )
            }
            RESET_APP -> {
                val activityManager = context.getSystemService<ActivityManager>()
                activityManager?.clearApplicationUserData()
            }
            RESET_SETTINGS -> {
                // TODO Add separate feature states
                context.extensionSettingsStore.updateData {
                    // Recreate the DataStore with default values
                    ExtensionSettings(
                        phoneSeparationNotis = false
                    )
                }
            }
            REQUEST_UPDATE_CAPABILITIES -> {
                CapabilityUpdater(context, discoveryClient).updateCapabilities()
            }
        }
    }
}
