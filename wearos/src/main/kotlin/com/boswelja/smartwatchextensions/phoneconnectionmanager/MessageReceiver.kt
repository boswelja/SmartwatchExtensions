package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.common.EmptySerializer
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.smartwatchextensions.common.connection.Messages.RESET_APP
import com.boswelja.smartwatchextensions.common.versioning.Version
import com.boswelja.smartwatchextensions.common.versioning.VersionSerializer
import com.boswelja.smartwatchextensions.discoveryClient
import com.boswelja.smartwatchextensions.dndsync.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.dndsync.REQUEST_SDK_INT_PATH
import com.boswelja.smartwatchextensions.extensions.SettingsSerializer
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.messageClient
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage

class MessageReceiver : MessageReceiver<Nothing?>(
    EmptySerializer(
        messagePaths = setOf(
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
            REQUEST_APP_VERSION,
            REQUEST_SDK_INT_PATH,
            RESET_APP,
            CLEAR_PREFERENCES,
            REQUEST_UPDATE_CAPABILITIES
        )
    )
) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Nothing?>) {
        when (message.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasDnDAccess = context.getSystemService<NotificationManager>()!!
                    .isNotificationPolicyAccessGranted
                context.messageClient(listOf()).sendMessage(
                    context.discoveryClient().pairedPhone()!!,
                    Message(
                        REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                        hasDnDAccess
                    )
                )
            }
            REQUEST_APP_VERSION -> {
                val version = Version(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
                context.messageClient(listOf(VersionSerializer)).sendMessage(
                    context.discoveryClient().pairedPhone()!!,
                    Message(REQUEST_APP_VERSION, version)
                )
            }
            RESET_APP -> {
                val activityManager = context.getSystemService<ActivityManager>()
                activityManager?.clearApplicationUserData()
            }
            CLEAR_PREFERENCES -> {
                context.extensionSettingsStore.updateData {
                    // Recreate the DataStore with default values
                    SettingsSerializer().defaultValue
                }
            }
            REQUEST_UPDATE_CAPABILITIES -> {
                CapabilityUpdater(context).updateCapabilities()
            }
        }
    }
}
