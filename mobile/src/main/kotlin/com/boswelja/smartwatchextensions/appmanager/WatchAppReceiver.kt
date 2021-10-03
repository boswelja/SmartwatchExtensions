package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.smartwatchextensions.appmanager.database.DbApp
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class WatchAppReceiver : MessageReceiver<AppList>(AppListSerializer) {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppList>) {
        WatchAppDatabase.getInstance(context).apps().also { db ->
            db.removeForWatch(message.sourceUid)
            message.data.apps
                .map { DbApp(message.sourceUid, it) }
                .also { db.add(it) }
        }
    }
}
