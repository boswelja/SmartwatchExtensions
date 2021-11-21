package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.serialization.MessageReceiver
import com.boswelja.watchconnection.wear.message.MessageClient
import okio.ByteString.Companion.toByteString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving app cache validation requests.
 */
class AppManagerCacheValidatorReceiver :
    MessageReceiver<AppVersions>(CacheValidationSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<AppVersions>
    ) {
        // Get a list of apps installed on this device, and format for cache validation.
        val currentPackages = context.packageManager
            .getInstalledPackages(PackageManager.GET_PERMISSIONS)

        val addedApps = getAddedPackages(context, currentPackages, message.data.versions)
        val updatedApps = getUpdatedPackages(context, currentPackages, message.data.versions)
        val removedApps = getRemovedPackages(currentPackages, message.data.versions)
        sendAppChanges(
            message.sourceUid,
            addedApps,
            updatedApps,
            removedApps
        )
        sendAllIcons(
            context,
            message.sourceUid,
            addedApps.apps + updatedApps.apps
        )
    }

    /**
     * Send all apps installed to the companion phone with a given ID.
     */
    private suspend fun sendAppChanges(
        targetUid: String,
        addedApps: AppList,
        updatedApps: AppList,
        removedApps: RemovedApps
    ) {
        // Let the phone know what we're doing
        messageClient.sendMessage(
            targetUid,
            Message(APP_SENDING_START, null)
        )

        val hasAddedApps = addedApps.isNotEmpty()
        val hasUpdatedApps = updatedApps.isNotEmpty()
        val hasRemovedApps = removedApps.isNotEmpty()

        if (hasAddedApps || hasUpdatedApps) {
            val addedOrUpdatedMessageHandler = MessageHandler(AddedOrUpdatedAppsSerializer, messageClient)
            if (hasAddedApps) {
                addedOrUpdatedMessageHandler.sendMessage(
                    targetUid,
                    Message(
                        ADDED_APPS,
                        addedApps
                    )
                )
            }
            if (hasUpdatedApps) {
                addedOrUpdatedMessageHandler.sendMessage(
                    targetUid,
                    Message(
                        UPDATED_APPS,
                        updatedApps
                    )
                )
            }
        }
        if (hasRemovedApps) {
            val removedAppMessageHandler = MessageHandler(RemovedAppsSerializer, messageClient)
            removedAppMessageHandler.sendMessage(
                targetUid,
                Message(
                    REMOVED_APPS,
                    removedApps
                )
            )
        }

        // Send a message notifying the phone of a successful operation
        messageClient.sendMessage(
            targetUid,
            Message(APP_SENDING_COMPLETE, null)
        )
    }

    private suspend fun sendAllIcons(
        context: Context,
        targetUid: String,
        allApps: List<App>
    ) {
        val messageHandler = MessageHandler(AppIconSerializer, messageClient)
        allApps.forEach { app ->
            try {
                // Load icon
                val drawable = context.packageManager.getApplicationIcon(app.packageName)
                val bitmap = drawable.toBitmap()
                val bytes = bitmap.toByteArray()
                messageHandler.sendMessage(
                    targetUid,
                    Message(
                        APP_ICON,
                        AppIcon(
                            app.packageName,
                            bytes.toByteString()
                        )
                    )
                )
            } catch (_: Exception) { }
        }
    }

    private fun getAddedPackages(
        context: Context,
        currentPackages: List<PackageInfo>,
        cachedPackages: List<AppVersion>
    ): AppList {
        val addedApps = currentPackages
            .filter { packageInfo ->
                cachedPackages.none { packageInfo.packageName == it.packageName }
            }
            .map { it.toApp(context.packageManager) }
        return AppList(addedApps)
    }

    private fun getUpdatedPackages(
        context: Context,
        currentPackages: List<PackageInfo>,
        cachedPackages: List<AppVersion>
    ): AppList {
        val addedApps = currentPackages
            .filter { packageInfo ->
                val longVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                cachedPackages.any {
                    packageInfo.packageName == it.packageName && longVersionCode != it.versionCode
                }
            }
            .map { it.toApp(context.packageManager) }
        return AppList(addedApps)
    }

    private fun getRemovedPackages(
        currentPackages: List<PackageInfo>,
        cachedPackages: List<AppVersion>
    ): RemovedApps {
        val removedPackages = cachedPackages
            .map { it.packageName }
            .filter { cachedApp ->
                currentPackages.none { cachedApp == it.packageName }
            }
        return RemovedApps(removedPackages)
    }
}