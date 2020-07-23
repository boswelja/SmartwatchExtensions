package com.boswelja.devicemanager.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class BitmapDataObject(bitmap: Bitmap) : Serializable {
    var currentImage: Bitmap
    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        val stream = ByteArrayOutputStream()
        currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        out.writeInt(byteArray.size)
        out.write(byteArray)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val bufferLength = `in`.readInt()
        val byteArray = ByteArray(bufferLength)
        var pos = 0
        do {
            val read = `in`.read(byteArray, pos, bufferLength - pos)
            pos += if (read != -1) {
                read
            } else {
                break
            }
        } while (pos < bufferLength)
        currentImage = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength)
    }

    init {
        currentImage = bitmap
    }
}