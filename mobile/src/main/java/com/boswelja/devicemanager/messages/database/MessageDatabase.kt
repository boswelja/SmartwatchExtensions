package com.boswelja.devicemanager.messages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.devicemanager.common.RoomTypeConverters
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.messages.Message

@Database(entities = [Message::class], version = 5)
@TypeConverters(RoomTypeConverters::class)
abstract class MessageDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    /**
     * Deletes all messages that were previously dismissed.
     */
    fun clearMessageHistory() {
        messageDao().clearDismissedMessages()
    }

    companion object : SingletonHolder<MessageDatabase, Context>({ context ->
        Room.databaseBuilder(context, MessageDatabase::class.java, "messages-db")
            .apply {
                fallbackToDestructiveMigration()
            }.build()
    })
}
