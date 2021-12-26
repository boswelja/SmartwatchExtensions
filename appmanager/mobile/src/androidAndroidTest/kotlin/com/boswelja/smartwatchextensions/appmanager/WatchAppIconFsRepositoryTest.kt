package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatchAppIconFsRepositoryTest {

    private lateinit var context: Context
    private lateinit var targetFile: File

    private lateinit var appIconRepository: WatchAppIconFsRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        targetFile = context.filesDir
        appIconRepository = WatchAppIconFsRepository(targetFile)
    }

    @After
    fun tearDown() {
        targetFile.deleteRecursively()
    }

    @Test
    fun retrieveIconFor_returnsNullWhenNoIconFound(): Unit = runBlocking {
        val iconBytes = appIconRepository.retrieveIconFor("uid", "com.package")
        assertNull(iconBytes)
    }

    @Test
    fun removeIconFor_returnsFalseWhenNoChangesWereMade(): Unit = runBlocking {
        val result = appIconRepository.removeIconFor("uid", "com.package")
        assertFalse(result)
    }

    @Test
    fun removeIconFor_returnsTrueWhenIconIsRemoved(): Unit = runBlocking {
        val watchUid = "uid"
        val packageName = "com.my.package"

        // Store an icon
        val bitmap = context.getDrawable(R.drawable.ic_framerate_30)!!.toBitmap(width = 100, height = 100)
        val bytes = bitmap.toByteArray()
        appIconRepository.storeIconFor(watchUid, packageName, bytes)

        // Remove it and check the result
        val result = appIconRepository.removeIconFor("uid", "com.package")
        assertTrue(result)
    }

    @Test
    fun storeIconFor_storesRetrievableIcons(): Unit = runBlocking {
        val watchUid = "uid"
        val packageName = "com.my.package"
        val bitmap = context.getDrawable(R.drawable.ic_framerate_30)!!.toBitmap(width = 100, height = 100)
        val bytes = bitmap.toByteArray()
        appIconRepository.storeIconFor(watchUid, packageName, bytes)

        // Retrieve the bytes and decode
        val readBytes = appIconRepository.retrieveIconFor(watchUid, packageName)
        BitmapFactory.decodeByteArray(readBytes, 0, readBytes!!.size)
        assertTrue { readBytes.contentEquals(bytes) }
    }

    @Test
    fun storeIconFor_overwritesExistingIcon(): Unit = runBlocking {
        val watchUid = "uid"
        val packageName = "com.my.package"
        // Store an initial icon
        appIconRepository.storeIconFor(
            watchUid,
            packageName,
            context.getDrawable(R.drawable.ic_framerate_30)!!.toBitmap(width = 100, height = 100).toByteArray()
        )
        // Store the updated icon
        val bitmap = context.getDrawable(R.drawable.ic_framerate_60)!!.toBitmap(width = 100, height = 100)
        val bytes = bitmap.toByteArray()
        appIconRepository.storeIconFor(watchUid, packageName, bytes)

        // Retrieve the bytes and decode
        val readBytes = appIconRepository.retrieveIconFor(watchUid, packageName)
        BitmapFactory.decodeByteArray(readBytes, 0, readBytes!!.size)
        assertTrue { readBytes.contentEquals(bytes) }
    }
}
