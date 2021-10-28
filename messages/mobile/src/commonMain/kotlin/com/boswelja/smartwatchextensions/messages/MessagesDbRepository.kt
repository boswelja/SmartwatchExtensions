package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A [MessagesRepository] implementation backed by a SQLDelight database.
 */
class MessagesDbRepository(
    private val database: MessageDatabase,
    private val dispatcher: CoroutineContext
) : MessagesRepository {

    override suspend fun insert(message: Message, sourceUid: String?) {
        withContext(dispatcher) {
            database.messageQueries.insert(
                id = null,
                source_uid = sourceUid,
                icon = message.icon,
                title = message.title,
                text = message.text,
                action = message.action,
                timestamp = message.timestamp,
                archived = false
            )
        }
    }

    override suspend fun archive(id: Long) {
        withContext(dispatcher) {
            database.messageQueries.setArchived(true, id)
        }
    }

    override suspend fun deleteArchived() {
        withContext(dispatcher) {
            database.messageQueries.deleteArchived()
        }
    }

    override suspend fun deleteForSource(sourceUid: String) {
        withContext(dispatcher) {
            database.messageQueries.deleteForSource(sourceUid)
        }
    }

    override fun getAllWhere(archived: Boolean): Flow<List<DisplayMessage>> =
        database.messageQueries
            .getAll(archived) {
                id: Long,
                source_uid: String?,
                icon: Message.Icon,
                title: String,
                text: String,
                action: Message.Action,
                timestamp: Long ->
                DisplayMessage(
                    id,
                    source_uid,
                    icon,
                    title,
                    text,
                    action,
                    timestamp
                )
            }
            .asFlow()
            .mapToList()
}
