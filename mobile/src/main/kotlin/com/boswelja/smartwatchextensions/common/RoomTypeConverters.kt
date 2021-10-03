package com.boswelja.smartwatchextensions.common

import androidx.room.TypeConverter
import java.util.UUID

class RoomTypeConverters {

    @TypeConverter
    fun uuidFromString(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(value: UUID?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun stringListToString(value: List<String>?): String? {
        return value?.joinToString("|")
    }

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? {
        return value?.split("|")
    }
}
