package com.boswelja.devicemanager.common

import androidx.room.TypeConverter
import com.boswelja.devicemanager.messages.Message

class RoomTypeConverters {
    @TypeConverter
    fun actionFromString(str: String?): Message.Action? {
        return if (str != null) {
            Message.Action.valueOf(str)
        } else {
            null
        }
    }

    @TypeConverter
    fun actionToString(action: Message.Action?): String? {
        return action?.toString()
    }
}
