package com.boswelja.smartwatchextensions.common

import androidx.room.TypeConverter
import com.boswelja.smartwatchextensions.messages.Message
import com.google.gson.Gson
import java.util.UUID

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

    @TypeConverter
    fun iconFromString(str: String?): Message.Icon? {
        return if (str != null) {
            Message.Icon.valueOf(str)
        } else {
            null
        }
    }

    @TypeConverter
    fun iconToString(icon: Message.Icon?): String? {
        return icon?.toString()
    }

    @TypeConverter
    fun uuidFromString(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(value: UUID?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun stringArrayToString(value: Array<String>?): String? {
        return value?.let {
            val gson = Gson()
            gson.toJson(value)
        }
    }

    @TypeConverter
    fun stringToStringArray(value: String?): Array<String>? {
        return value?.let {
            val gson = Gson()
            gson.fromJson(value, Array<String>::class.java)
        }
    }
}
