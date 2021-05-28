package com.boswelja.smartwatchextensions.messages.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun send(message: Message): Long

    @Query("UPDATE messages SET deleted = 1 WHERE id = :messageId")
    suspend fun dismiss(messageId: Long)

    @Query("UPDATE messages SET deleted = 0 WHERE id = :messageId")
    suspend fun restore(messageId: Long)

    @Query("DELETE FROM messages WHERE deleted")
    suspend fun deleteDismissed()

    @Query("SELECT * FROM messages WHERE NOT deleted ORDER BY timestamp DESC")
    fun activeMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE deleted ORDER BY timestamp DESC")
    fun dismissedMessages(): Flow<List<Message>>

    @Query("SELECT COUNT(*) FROM messages WHERE NOT deleted")
    fun activeMessagesCount(): Flow<Int>
}
