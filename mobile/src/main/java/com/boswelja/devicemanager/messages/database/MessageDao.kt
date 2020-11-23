package com.boswelja.devicemanager.messages.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.messages.Message

@Dao
interface MessageDao {

    @Insert
    fun createMessage(message: Message)

    @Query("UPDATE messages SET deleted = 1 WHERE id = :messageId")
    fun dismissMessage(messageId: Long)

    @Query("SELECT * FROM messages WHERE NOT deleted")
    fun getActiveMessages(): PagingSource<Int, Message>

    @Query("SELECT * FROM messages WHERE deleted")
    fun getDismissedMessages(): PagingSource<Int, Message>
}
