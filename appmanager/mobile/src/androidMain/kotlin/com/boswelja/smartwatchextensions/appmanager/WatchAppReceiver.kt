package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabaseLoader
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class WatchAppReceiver : MessageReceiver<AppList>(AppListSerializer) {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppList>) {
        val repository = WatchAppDbRepository(WatchAppDatabaseLoader(context).createDatabase())
        val apps = message.data.apps.map {
            WatchAppDetails(
                message.sourceUid,
                it.packageName,
                null,
                it.label,
                it.version,
                0,
                it.isSystemApp,
                it.hasLaunchActivity,
                it.isEnabled,
                it.installTime,
                it.lastUpdateTime,
                it.requestedPermissions
            )
        }
        repository.updateAll(apps)
    }
}
