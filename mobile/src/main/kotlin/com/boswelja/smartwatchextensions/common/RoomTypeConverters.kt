package com.boswelja.smartwatchextensions.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
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

    @TypeConverter
    fun bitmapToByteArray(value: Bitmap?): ByteArray? {
        return value?.let {
            ByteArrayOutputStream().use { stream ->
                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        }
    }

    @TypeConverter
    fun byteArrayToBitmap(value: ByteArray?): Bitmap? {
        return value?.let {
            BitmapFactory.decodeByteArray(value, 0, value.size)
        }
    }
}
