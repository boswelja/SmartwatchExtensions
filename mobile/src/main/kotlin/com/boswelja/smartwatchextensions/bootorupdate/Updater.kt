package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import com.boswelja.migration.Migrator
import com.boswelja.smartwatchextensions.appStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Updater(private val context: Context) : Migrator(
    currentVersion = 1,
    migrations = listOf()
) {
    override suspend fun getOldVersion(): Int {
        return context.appStateStore.data.map { it.lastAppVersion }.first()
    }
}
