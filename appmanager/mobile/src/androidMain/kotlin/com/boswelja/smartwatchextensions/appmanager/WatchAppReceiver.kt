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

        val apps = message.data.mapToWatchAppDetails(message.sourceUid)
        repository.updateAll(apps)
    }
}
