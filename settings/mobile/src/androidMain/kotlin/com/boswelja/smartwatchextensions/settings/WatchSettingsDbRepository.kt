package com.boswelja.smartwatchextensions.settings

import com.boswelja.smartwatchextensions.settings.database.BoolSetting
import com.boswelja.smartwatchextensions.settings.database.IntSetting
import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WatchSettingsDbRepository(
    private val database: WatchSettingsDatabase
) : WatchSettingsRepository {
    override suspend fun putBoolean(watchId: String, key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            database.boolSettingQueries.update(
                BoolSetting(watchId, key, value)
            )
        }
    }

    override suspend fun putInt(watchId: String, key: String, value: Int) {
        withContext(Dispatchers.IO) {
            database.intSettingQueries.update(
                IntSetting(watchId, key, value)
            )
        }
    }

    override fun getBoolean(watchId: String, key: String, defaultValue: Boolean): Flow<Boolean> =
        database.boolSettingQueries
            .get(watchId, key)
            .asFlow()
            .mapToOne()

    override fun getInt(watchId: String, key: String, defaultValue: Int): Flow<Int> =
        database.intSettingQueries
            .get(watchId, key)
            .asFlow()
            .mapToOne()

    override fun getIdsWithBooleanSet(key: String, value: Boolean): Flow<List<String>> =
        database.boolSettingQueries
            .getIdsWithSetting(key, value)
            .asFlow()
            .mapToList()

    override fun getIdsWithIntSet(key: String, value: Int): Flow<List<String>> =
        database.intSettingQueries
            .getIdsWithSetting(key, value)
            .asFlow()
            .mapToList()

    override suspend fun deleteForWatch(watchId: String) {
        database.transaction {
            database.boolSettingQueries.deleteFor(watchId)
            database.intSettingQueries.deleteFor(watchId)
        }
    }
}
