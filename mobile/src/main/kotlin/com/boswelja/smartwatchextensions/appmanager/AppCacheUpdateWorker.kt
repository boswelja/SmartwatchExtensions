package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : BaseAppCacheUpdateWorker(context, workerParams), KoinComponent {

    private val watchManager: WatchManager by inject()
    private val messageClient: MessageClient by inject()

    override suspend fun onSendCacheState(targetUid: String, cacheHash: Int): Boolean {
        val watch = watchManager.getWatchById(targetUid).first() ?: return false
        val handler = MessageHandler(CacheValidationSerializer, messageClient)
        return handler.sendMessage(
            watch.uid,
            Message(
                VALIDATE_CACHE,
                cacheHash
            )
        )
    }
}
