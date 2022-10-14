package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.ConnectionMode
import com.boswelja.smartwatchextensions.wearableinterface.Watch
import com.boswelja.smartwatchextensions.wearableinterface.WatchRepository
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await

internal class WatchRepositoryImpl(context: Context) : WatchRepository {

    private val nodeClient = Wearable.getNodeClient(context)

    override fun getAllWatches(): Flow<List<Watch>> = flow {
        while (true) {
            val watches = nodeClient.connectedNodes.await().map {
                Watch(
                    it.id,
                    it.displayName,
                    if (it.isNearby) ConnectionMode.Bluetooth else ConnectionMode.Network
                )
            }
            emit(watches)
            delay(AllWatchSearchInterval)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getConnectionModeFor(watchId: String): Flow<ConnectionMode> = getAllWatches()
        .mapLatest {
            it.firstOrNull { it.uid == watchId }?.connectionMode ?: ConnectionMode.None
        }

    private companion object {
        private const val AllWatchSearchInterval = 5000L
    }
}
