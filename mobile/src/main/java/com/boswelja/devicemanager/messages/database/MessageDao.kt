/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.messages.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.messages.Message

@Dao
interface MessageDao {

    @Insert
    fun sendMessage(message: Message)

    @Query("SELECT * FROM messages")
    fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE NOT deleted")
    fun getActiveMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE deleted")
    fun getDeletedMessages(): List<Message>

    @Query("UPDATE messages SET deleted = 1 WHERE id = :id")
    fun deleteMessage(id: Int)

    @Query("UPDATE messages SET deleted = 0 WHERE id = :id")
    fun restoreMessage(id: Int)


    @Query("SELECT * FROM messages")
    fun getAllMessagesObservable(): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE NOT deleted")
    fun getActiveMessagesObservable(): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE deleted")
    fun getDeletedMessagesObservable(): LiveData<List<Message>>

}
