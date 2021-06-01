package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.ALL_APPS
import com.boswelja.smartwatchextensions.common.appmanager.decompressFromByteArray
import com.boswelja.watchconnection.core.MessageReceiver
import java.util.UUID
import timber.log.Timber

class AppReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(
        context: Context,
        sourceWatchId: UUID,
        message: String,
        data: ByteArray?
    ) {
        if (data != null) {
            when (message) {
                ALL_APPS -> {
                    val allApps = decompressFromByteArray(data)
                    Timber.d("Received %s apps", allApps.count())
                    val database = WatchAppDatabase.getInstance(context)
                    database.apps().removeForWatch(sourceWatchId)
                    allApps.forEach { watchApp ->
                        database.apps().add(App(sourceWatchId, watchApp))
                    }
                }
            }
        }
    }
}
