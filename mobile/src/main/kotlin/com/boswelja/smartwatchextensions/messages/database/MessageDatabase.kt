package com.boswelja.smartwatchextensions.messages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.smartwatchextensions.common.RoomTypeConverters
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.messages.Message

@Database(entities = [Message::class], version = 5)
@TypeConverters(RoomTypeConverters::class)
abstract class MessageDatabase : RoomDatabase() {

    abstract fun messages(): MessageDao

    companion object : SingletonHolder<MessageDatabase, Context>({ context ->
        Room.databaseBuilder(context, MessageDatabase::class.java, "messages-db")
            .apply {
                fallbackToDestructiveMigration()
            }.build()
    })
}
