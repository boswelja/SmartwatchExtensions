package com.boswelja.smartwatchextensions.common.appmanager

import java.util.zip.Deflater
import java.util.zip.Inflater

fun ByteArray.compress(
    level: Int = Deflater.BEST_COMPRESSION
): ByteArray {
    // Start compression
    val compressor = Deflater(level)
    compressor.setInput(this)
    compressor.finish()

    // Get compressed bytes
    val result = ByteArray(size) // Assume we'll never exceed the size of the original bytes
    val totalBytes = compressor.deflate(result)
    compressor.end()

    return result.copyOf(totalBytes)
}

fun ByteArray.decompress(): ByteArray {
    // Decompress bytes
    val decompressor = Inflater()
    decompressor.setInput(this)
    val decompressedBytes = ByteArray(Short.MAX_VALUE.toInt())
    val resultLength = decompressor.inflate(decompressedBytes)
    decompressor.end()
    return decompressedBytes.copyOf(resultLength)
}
