package com.boswelja.devicemanager.messages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.messages.Message

@Database(entities = [Message::class], version = 5)
abstract class MessageDatabase : RoomDatabase() {

    abstract fun dao(): MessageDao

    companion object {
        private var INSTANCE: MessageDatabase? = null

        /**
         * Gets an instance of [MessageDatabase].
         * @param context [Context].
         * @return The [MessageDatabase] instance.
         */
        fun get(context: Context): MessageDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE =
                        Room.databaseBuilder(context, MessageDatabase::class.java, "messages-db")
                            .fallbackToDestructiveMigration()
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}
