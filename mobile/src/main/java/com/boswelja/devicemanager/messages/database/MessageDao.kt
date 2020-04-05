package com.boswelja.devicemanager.messages.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.messages.Message

@Dao
interface MessageDao {

    @Insert
    fun sendMessage(message: Message)

    @Query("SELECT * FROM messages")
    fun getAllMessages(): Array<Message>

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    fun getMessage(messageId: Int): Message?

    @Query("SELECT * FROM messages WHERE NOT deleted")
    fun getActiveMessages(): Array<Message>

    @Query("SELECT * FROM messages WHERE deleted")
    fun getDeletedMessages(): Array<Message>

    @Query("UPDATE messages SET deleted = 1 WHERE id = :id")
    fun deleteMessage(id: Int)

    @Query("UPDATE messages SET deleted = 0 WHERE id = :id")
    fun restoreMessage(id: Int)
}