package com.boswelja.smartwatchextensions.wearableinterface

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

interface ChannelManager {

    @Throws(IOException::class)
    suspend fun sendDataTo(watchId: String, path: String, block: suspend (OutputStream) -> Unit)

    @Throws(IOException::class)
    suspend fun receiveDataFrom(watchId: String, path: String, block: suspend (InputStream) -> Unit)
}
