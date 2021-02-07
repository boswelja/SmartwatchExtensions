package com.boswelja.devicemanager.messages.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.messages.Message

@Dao
interface MessageDao {

    @Insert
    fun createMessage(message: Message): Long

    @Query("UPDATE messages SET deleted = 1 WHERE id = :messageId")
    fun dismissMessage(messageId: Long)

    @Query("SELECT * FROM messages WHERE NOT deleted ORDER BY timestamp DESC")
    fun getActiveMessages(): PagingSource<Int, Message>

    @Query("SELECT * FROM messages WHERE deleted ORDER BY timestamp DESC")
    fun getDismissedMessages(): PagingSource<Int, Message>

    @Query("SELECT COUNT(*) FROM messages WHERE NOT deleted")
    fun getActiveMessagesCount(): LiveData<Int>

    @Query("UPDATE messages SET deleted = 0 WHERE id = :messageId")
    fun restoreMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE deleted")
    fun clearDismissedMessages()
}
