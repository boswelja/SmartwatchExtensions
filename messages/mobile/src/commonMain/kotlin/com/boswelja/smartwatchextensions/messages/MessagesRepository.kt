package com.boswelja.smartwatchextensions.messages

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing Messages.
 */
interface MessagesRepository {

    /**
     * Insert a message into the store.
     */
    suspend fun insert(message: Message, sourceUid: String?)

    /**
     * Mark a message with the given ID as archived.
     * @param id The ID of the message to archive.
     */
    suspend fun archive(id: Long)

    /**
     * Delete all archived messages from the database
     */
    suspend fun deleteArchived()

    /**
     * Delete all messages from a given source.
     * @param sourceUid The UID of the source whose messages should be deleted.
     */
    suspend fun deleteForSource(sourceUid: String)

    /**
     * Flow a list of messages with the matching archive state.
     * @param archived True to collect archived messages, false otherwise.
     */
    fun getAllWhere(archived: Boolean): Flow<List<DisplayMessage>>
}
