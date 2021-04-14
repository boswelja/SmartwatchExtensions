package com.boswelja.smartwatchextensions.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class SerializableBitmap(var bitmap: Bitmap) : Serializable {

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            out.writeInt(byteArray.size)
            out.write(byteArray)
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val bufferLength = `in`.readInt()
        val buffer = ByteArray(bufferLength)
        var pos = 0
        do {
            val read = `in`.read(buffer, pos, bufferLength - pos)
            pos +=
                if (read != -1) {
                    read
                } else {
                    break
                }
        } while (pos < bufferLength)
        bitmap = BitmapFactory.decodeByteArray(buffer, 0, bufferLength)
    }
}
