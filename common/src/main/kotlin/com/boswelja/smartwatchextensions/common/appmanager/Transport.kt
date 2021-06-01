package com.boswelja.smartwatchextensions.common.appmanager

import java.util.zip.Deflater
import java.util.zip.Inflater

fun App.compressToByteArray(): ByteArray {
    // Get ByteArray
    val bytes = toByteArray()
    // Start compression
    val compressor = Deflater(Deflater.BEST_COMPRESSION)
    compressor.setInput(bytes)
    compressor.finish()

    // Get compressed bytes
    val result = ByteArray(bytes.size) // Assume we'll never exceed the size of the original bytes
    val totalBytes = compressor.deflate(result)
    compressor.end()

    return result.copyOf(totalBytes)
}

fun decompressFromByteArray(data: ByteArray): App {
    // Decompress bytes
    val decompressor = Inflater()
    decompressor.setInput(data)
    val decompressedBytes = ByteArray(Short.MAX_VALUE.toInt())
    val resultLength = decompressor.inflate(decompressedBytes)
    decompressor.end()
    val appBytes = decompressedBytes.copyOf(resultLength)

    // Return original list
    return App.fromByteArray(appBytes)
}
