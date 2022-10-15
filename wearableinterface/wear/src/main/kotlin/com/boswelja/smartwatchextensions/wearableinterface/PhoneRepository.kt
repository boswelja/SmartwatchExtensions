package com.boswelja.smartwatchextensions.wearableinterface

interface PhoneRepository {
    suspend fun getPairedPhone(): Phone
}

data class Phone(
    val uid: String,
    val name: String,
    val connectionMode: ConnectionMode
)

enum class ConnectionMode {
    Network,
    Bluetooth
}
