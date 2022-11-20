package com.boswelja.smartwatchextensions.core.watches.registered

import com.boswelja.smartwatchextensions.core.devicemanagement.database.RegisteredWatch
import com.boswelja.smartwatchextensions.core.devicemanagement.database.RegisteredWatchDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A [RegisteredWatchRepository] implementation backed by a SQLDelight database.
 */
class RegisteredWatchDbRepository(
    private val database: RegisteredWatchDatabase,
    private val dispatcher: CoroutineContext
) : RegisteredWatchRepository {

    override val registeredWatches: Flow<List<RegisteredWatch>>
        get() = database.registeredWatchQueries
            .getAll()
            .asFlow()
            .mapToList()

    override suspend fun registerWatch(id: String, name: String) {
        withContext(dispatcher) {
            database.registeredWatchQueries.insert(
                RegisteredWatch(
                    id,
                    name,
                    ""
                )
            )
        }
    }

    override suspend fun deregisterWatch(id: String) {
        withContext(dispatcher) {
            database.registeredWatchQueries.delete(id)
        }
    }

    override suspend fun renameWatch(id: String, newName: String) {
        withContext(dispatcher) {
            database.registeredWatchQueries.rename(newName, id)
        }
    }

    override fun getWatchById(id: String): Flow<RegisteredWatch?> = database.registeredWatchQueries
        .get(id)
        .asFlow()
        .mapToOneOrNull()
}
