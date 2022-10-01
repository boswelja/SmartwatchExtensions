package com.boswelja.smartwatchextensions.watchmanager.domain

import com.boswelja.smartwatchextensions.common.Version

interface WatchVersionRepository {

    suspend fun getWatchVersion(watchId: String): Version
}
