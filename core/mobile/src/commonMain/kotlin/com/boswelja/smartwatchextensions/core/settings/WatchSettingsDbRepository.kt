package com.boswelja.smartwatchextensions.core.settings

import com.boswelja.smartwatchextensions.core.settings.database.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.database.IntSetting
import com.boswelja.smartwatchextensions.core.settings.database.WatchSettingsDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A [WatchSettingsRepository] implementation backed by a SQLDelight database.
 */
class WatchSettingsDbRepository(
    private val database: WatchSettingsDatabase,
    private val dispatcher: CoroutineContext
) : WatchSettingsRepository {

    override suspend fun putBoolean(watchId: String, key: String, value: Boolean) {
        withContext(dispatcher) {
            database.boolSettingQueries.update(
                BoolSetting(watchId, key, value)
            )
        }
    }

    override suspend fun putInt(watchId: String, key: String, value: Int) {
        withContext(dispatcher) {
            database.intSettingQueries.update(
                IntSetting(watchId, key, value)
            )
        }
    }

    override fun getBoolean(watchId: String, key: String, defaultValue: Boolean): Flow<Boolean> =
        database.boolSettingQueries
            .get(watchId, key)
            .asFlow()
            .mapToOneOrNull()
            .map { it ?: defaultValue }

    override fun getInt(watchId: String, key: String, defaultValue: Int): Flow<Int> =
        database.intSettingQueries
            .get(watchId, key)
            .asFlow()
            .mapToOneOrNull()
            .map { it ?: defaultValue }

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
