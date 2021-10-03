package com.boswelja.smartwatchextensions.common

import androidx.room.TypeConverter

class RoomTypeConverters {

    @TypeConverter
    fun stringListToString(value: List<String>?): String? {
        return value?.joinToString("|")
    }

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? {
        return value?.split("|")
    }
}
