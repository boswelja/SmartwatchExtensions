package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A [WatchAppIconRepository] implementation that stores icons on the file system.
 */
class WatchAppIconFsRepository(
    private val rootFolder: File
): WatchAppIconRepository {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun storeIconFor(watchId: String, packageName: String, iconBytes: ByteArray) {
        withContext(Dispatchers.IO) {
            val targetFile = getTargetFile(watchId, packageName)
            targetFile.createNewFile()
            targetFile.writeBytes(iconBytes)
        }
    }

    override suspend fun removeIconFor(watchId: String, packageName: String): Boolean {
        val targetFile = getTargetFile(watchId, packageName)
        return targetFile.delete()
    }

    override suspend fun retrieveIconFor(watchId: String, packageName: String): ByteArray? {
        val targetFile = getTargetFile(watchId, packageName)
        return if (targetFile.exists()) {
            targetFile.readBytes()
        } else {
            null
        }
    }

    private fun getTargetFile(watchId: String, packageName: String): File {
        val folder = File(rootFolder, watchId)
        folder.mkdirs()
        return File(folder, packageName)
    }
}
