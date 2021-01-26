package com.boswelja.devicemanager.common

import androidx.room.TypeConverter
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.watchmanager.item.Watch

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
    fun platformToString(platform: Watch.Platform): String {
        return platform.name
    }

    @TypeConverter
    fun platformFromInt(name: String): Watch.Platform {
        return Watch.Platform.valueOf(name)
    }
}
