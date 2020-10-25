/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.messages.database

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.messages.Message

@Database(entities = [Message::class], version = 4)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    /**
     * Get the true count of all active messages.
     * @return The active message count.
     */
    fun countMessages(): Int {
        return messageDao().getActiveMessages().size
    }

    /**
     * Update the tracked message count in [SharedPreferences].
     * @param sharedPreferences The [SharedPreferences] instance to use for storing the tracked
     * message count.
     */
    fun updateMessageCount(sharedPreferences: SharedPreferences) {
        val trueCount = countMessages()
        sharedPreferences.edit { putInt(MESSAGE_COUNT_KEY, trueCount) }
    }

    /**
     * Store a new message in the [MessageDatabase], and update the tracked message counter in
     * [SharedPreferences].
     * @param sharedPreferences The [SharedPreferences] instance to update the tracked counter.
     * @param message The new message to send.
     * @return true if the message was successfully stored, false otherwise.
     */
    fun sendMessage(sharedPreferences: SharedPreferences, message: Message): Boolean {
        messageDao().sendMessage(message)
        sharedPreferences.edit {
            putInt(MESSAGE_COUNT_KEY, sharedPreferences.getInt(MESSAGE_COUNT_KEY, 0) + 1)
        }
        return true
    }

    /**
     * Mark a [Message] as deleted and update the tracked message counter.
     * @param sharedPreferences The [SharedPreferences] instance to update the tracked counter.
     * @param message The deleted [Message].
     * @return true if the message was deleted successfully, false otherwise.
     */
    fun deleteMessage(sharedPreferences: SharedPreferences, message: Message): Boolean {
        if (isOpen) {
            messageDao().deleteMessage(message.id)
            sharedPreferences.edit {
                putInt(MESSAGE_COUNT_KEY, sharedPreferences.getInt(MESSAGE_COUNT_KEY, 1) - 1)
            }
            return true
        }
        return false
    }

    /**
     * Restores a [Message] from it's deleted state, and updates the tracked message counter.
     * @param sharedPreferences The [SharedPreferences] instance to update the tracked counter.
     * @param message The [Message] to restore.
     * @return true if the message was successfully restored, false otherwise.
     */
    fun restoreMessage(sharedPreferences: SharedPreferences, message: Message): Boolean {
        if (isOpen) {
            messageDao().restoreMessage(message.id)
            sharedPreferences.edit {
                putInt(MESSAGE_COUNT_KEY, sharedPreferences.getInt(MESSAGE_COUNT_KEY, 0) + 1)
            }
            return true
        }
        return false
    }

    override fun close() {
        INSTANCE = null
        super.close()
    }

    companion object {
        private var INSTANCE: MessageDatabase? = null

        const val MESSAGE_COUNT_KEY = "message_count"

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
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}
