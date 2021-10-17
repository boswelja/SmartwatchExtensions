package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.kodein.di.DIAware
import org.kodein.di.LateInitDI
import org.kodein.di.instance

class WatchAppReceiver : MessageReceiver<AppList>(AppListSerializer), DIAware {
    override val di = LateInitDI()

    private val repository: WatchAppRepository by instance()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppList>) {
        di.baseDI = (context.applicationContext as DIAware).di

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
