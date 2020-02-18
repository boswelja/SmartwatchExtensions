package com.boswelja.devicemanager.messages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.messages.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Message::class], version = 1)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    fun countMessages(): Int {
        return messageDao().getActiveMessages().size
    }

    fun getActiveMessages(): Array<Message> {
        return messageDao().getActiveMessages()
    }

    fun getDeletedMessages(): Array<Message> {
        return messageDao().getDeletedMessages()
    }

    fun messageExists(messageId: Int): Boolean {
        return messageDao().getMessage(messageId) != null
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
