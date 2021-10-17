package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.common.EmptySerializer
import com.boswelja.smartwatchextensions.devicemanagement.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.smartwatchextensions.devicemanagement.RESET_APP
import com.boswelja.smartwatchextensions.dndsync.REQUEST_SDK_INT_PATH
import com.boswelja.smartwatchextensions.extensions.SettingsSerializer
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.settings.RESET_SETTINGS
import com.boswelja.smartwatchextensions.versionsync.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.versionsync.Version
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.kodein.di.DIAware
import org.kodein.di.LateInitDI
import org.kodein.di.instance

class MessageReceiver :
    MessageReceiver<Nothing?>(
        EmptySerializer(
            messagePaths = setOf(
                REQUEST_APP_VERSION,
                REQUEST_SDK_INT_PATH,
                RESET_APP,
                RESET_SETTINGS,
                REQUEST_UPDATE_CAPABILITIES
            )
        )
    ),
    DIAware {

    override val di = LateInitDI()

    private val messageClient: MessageClient by instance()
    private val discoveryClient: DiscoveryClient by instance()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Nothing?>) {
        di.baseDI = (context.applicationContext as DIAware).di

        when (message.path) {
            REQUEST_APP_VERSION -> {
                val version = Version(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
                messageClient.sendMessage(
                    discoveryClient.pairedPhone()!!,
                    Message(REQUEST_APP_VERSION, version)
                )
            }
            RESET_APP -> {
                val activityManager = context.getSystemService<ActivityManager>()
                activityManager?.clearApplicationUserData()
            }
            RESET_SETTINGS -> {
                context.extensionSettingsStore.updateData {
                    // Recreate the DataStore with default values
                    SettingsSerializer().defaultValue
                }
            }
            REQUEST_UPDATE_CAPABILITIES -> {
                CapabilityUpdater(context, discoveryClient).updateCapabilities()
            }
        }
    }
}
