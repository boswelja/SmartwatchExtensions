package com.boswelja.smartwatchextensions.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.boswelja.smartwatchextensions.messages.Message
import java.io.ByteArrayOutputStream
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
