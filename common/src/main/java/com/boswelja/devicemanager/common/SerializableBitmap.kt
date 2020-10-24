/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

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
