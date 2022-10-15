package com.boswelja.smartwatchextensions.appmanager

import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class AppManagerListenerService : WearableListenerService() {

    private val messageClient = Wearable.getMessageClient(this)

    override fun onMessageReceived(message: MessageEvent) {
        if (message.data.isEmpty()) return

        when (message.path) {
            RequestValidateCache -> {
                if (message.data.isEmpty()) return
                val appVersions = CacheValidationSerializer.deserialize(message.data)
                performCacheValidation(message.sourceNodeId, appVersions)
            }
            RequestUninstallPackage -> {
                val packageName = PackageNameSerializer.deserialize(message.data)
                startActivity(packageManager.requestUninstallIntent(packageName))
            }
            RequestOpenPackage -> {
                val packageName = PackageNameSerializer.deserialize(message.data)
                startActivity(packageManager.launchIntent(packageName))
            }
        }
    }

    private fun performCacheValidation(
        targetId: String,
        appVersions: AppVersions
    ) {

        // Get a list of apps installed on this device, and format for cache validation.
        val currentPackages = packageManager.getAllApps()

        val addedApps = getAddedPackages(currentPackages, appVersions.versions)
        val updatedApps = getUpdatedPackages(currentPackages, appVersions.versions)
        val removedApps = getRemovedPackages(currentPackages, appVersions.versions)
        sendAppChanges(
            targetId,
            addedApps,
            updatedApps,
            removedApps
        )
        sendAllIcons(
            targetId,
            addedApps.apps + updatedApps.apps
        )
    }
    /**
     * Send all apps installed to the companion phone with a given ID.
     */
    private fun sendAppChanges(
        targetUid: String,
        addedApps: AppList,
        updatedApps: AppList,
        removedApps: RemovedApps
    ) {
        // Let the phone know what we're doing
        messageClient.sendMessage(
            targetUid,
            NotifyAppSendingStart,
            null
        )

        val hasAddedApps = addedApps.isNotEmpty()
        val hasUpdatedApps = updatedApps.isNotEmpty()
        val hasRemovedApps = removedApps.isNotEmpty()

        if (hasAddedApps || hasUpdatedApps) {
            if (hasAddedApps) {
                messageClient.sendMessage(
                    targetUid,
                    AddedAppsList,
                    AddedOrUpdatedAppsSerializer.serialize(addedApps)
                )
            }
            if (hasUpdatedApps) {
                messageClient.sendMessage(
                    targetUid,
                    UpdatedAppsList,
                    AddedOrUpdatedAppsSerializer.serialize(addedApps)
                )
            }
        }
        if (hasRemovedApps) {
            messageClient.sendMessage(
                targetUid,
                RemovedAppsList,
                RemovedAppsSerializer.serialize(removedApps)
            )
        }

        // Send a message notifying the phone of a successful operation
        messageClient.sendMessage(
            targetUid,
            NotifyAppSendingComplete,
            null
        )
    }

    private fun sendAllIcons(
        targetUid: String,
        allApps: List<App>
    ) {
        allApps.forEach { app ->
            try {
                // Load icon
                val drawable = packageManager.getApplicationIcon(app.packageName)
                val bitmap = drawable.toBitmap()
                val bytes = bitmap.toByteArray()
                messageClient.sendMessage(
                    targetUid,
                    RawAppIcon,
                    AppIconSerializer.serialize(AppIcon(app.packageName, bytes))
                )
            } catch (_: Exception) { }
        }
    }

    internal fun getAddedPackages(
        currentPackages: List<App>,
        cachedPackages: List<AppVersion>
    ): AppList {
        val addedApps = currentPackages
            .filter { packageInfo ->
                cachedPackages.none { packageInfo.packageName == it.packageName }
            }
        return AppList(addedApps)
    }

    internal fun getUpdatedPackages(
        currentPackages: List<App>,
        cachedPackages: List<AppVersion>
    ): AppList {
        val addedApps = currentPackages
            .filter { packageInfo ->
                cachedPackages.any {
                    packageInfo.packageName == it.packageName && packageInfo.versionCode != it.versionCode
                }
            }
        return AppList(addedApps)
    }

    internal fun getRemovedPackages(
        currentPackages: List<App>,
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
