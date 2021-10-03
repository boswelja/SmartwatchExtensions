package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.smartwatchextensions.appmanager.database.DbApp
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.AppList
import com.boswelja.smartwatchextensions.common.appmanager.AppListSerializer
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.common.message.serialized.TypedMessageReceiver

class WatchAppReceiver : TypedMessageReceiver<AppList>(AppListSerializer) {
    override suspend fun onTypedMessageReceived(
        context: Context,
        message: ReceivedMessage<AppList>
    ) {
        WatchAppDatabase.getInstance(context).apps().also { db ->
            db.removeForWatch(message.sourceWatchID)
            message.data.apps
                .map { DbApp(message.sourceWatchID, it) }
                .also { db.add(it) }
        }
    }

    override suspend fun onDeserializeException(exception: Exception) {
        // TODO Notify the user their apps may be out of date
    }
}
