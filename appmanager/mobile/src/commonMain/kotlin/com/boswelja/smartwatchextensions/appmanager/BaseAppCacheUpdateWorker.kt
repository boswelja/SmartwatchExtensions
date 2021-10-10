package com.boswelja.smartwatchextensions.appmanager

expect abstract class BaseAppCacheUpdateWorker {
    abstract suspend fun onSendCacheState(
        targetUid: String,
        cacheHash: Int
    ): Boolean
}
