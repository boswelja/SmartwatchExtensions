package com.boswelja.smartwatchextensions.wearableinterface

import kotlinx.coroutines.flow.Flow

interface WatchRepository {

    fun getAllWatches(): Flow<List<Watch>>

    fun getConnectionModeFor(watchId: String): Flow<ConnectionMode>
}

data class Watch(
    val uid: String,
    val name: String,
    val connectionMode: ConnectionMode
)

enum class ConnectionMode {
    Network,
    Bluetooth,
    None
}
