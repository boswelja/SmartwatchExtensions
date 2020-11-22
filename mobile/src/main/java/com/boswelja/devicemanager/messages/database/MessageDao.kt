package com.boswelja.devicemanager.messages.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.messages.Message

@Dao
interface MessageDao {

    @Insert
    fun createMessage(message: Message)

    @Query("UPDATE messages SET deleted = 1 WHERE id = :messageId")
    fun dismissMessage(messageId: Int)

    @Query("SELECT * FROM messages WHERE NOT deleted")
    fun getActiveMessages(): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE deleted")
    fun getDismissedMessages(): LiveData<List<Message>>
}
