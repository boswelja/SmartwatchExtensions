package com.boswelja.devicemanager.messages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.messages.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Message::class], version = 2)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    fun countMessages(): Int {
        return messageDao().getActiveMessages().size
    }

    fun getActiveMessages(): List<Message> {
        return messageDao().getActiveMessages().sortedBy { it.timestamp }
    }

    fun getDeletedMessages(): List<Message> {
        return messageDao().getDeletedMessages().sortedBy { it.timestamp }
    }

    fun messageExists(messageId: Int): Boolean {
        return messageDao().getMessage(messageId) != null
    }

    fun sendMessage(message: Message): Boolean {
        if (isOpen) {
            messageDao().sendMessage(message)
            return true
        }
        return false
    }

    companion object {
        suspend fun open(context: Context): MessageDatabase {
            return withContext(Dispatchers.IO) {
                return@withContext Room.databaseBuilder(context, MessageDatabase::class.java, "messages-db")
                        .build()
            }
        }
    }
}
