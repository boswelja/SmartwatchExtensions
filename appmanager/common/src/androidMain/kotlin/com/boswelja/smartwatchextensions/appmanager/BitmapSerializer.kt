package com.boswelja.smartwatchextensions.appmanager

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream

/**
 * Serialize the Bitmap to a ByteArray. WEBP_LOSSLESS will be used where possible.
 */
fun Bitmap.toByteArray(
    quality: Int = 100
): ByteArray {
    // Use lossless webp where available
    val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSLESS
    } else {
        Bitmap.CompressFormat.PNG
    }
    return ByteArrayOutputStream(byteCount).use {
        compress(compressFormat, quality, it)
        it.toByteArray()
    }
}
