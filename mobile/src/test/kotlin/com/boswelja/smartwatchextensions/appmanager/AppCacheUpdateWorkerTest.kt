package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.boswelja.smartwatchextensions.WatchManagerTestRule
import com.boswelja.smartwatchextensions.appmanager.AppCacheUpdateWorker.Companion.EXTRA_WATCH_ID
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.watchconnection.core.Watch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import java.util.UUID
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AppCacheUpdateWorkerTest {

    @get:Rule
    val watchManagerRule = WatchManagerTestRule()

    private val watch = Watch(UUID.randomUUID(), "", "", "")

    private lateinit var context: Context

    private lateinit var appDatabase: WatchAppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Mock Watch Manager
        every {
            watchManagerRule.watchManager.getWatchById(watch.id)
        } returns flow { emit(watch.toDbWatch()) }

        // Set up dummy database
        appDatabase = Room.inMemoryDatabaseBuilder(context, WatchAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mockkObject(WatchAppDatabase.Companion)
        every {
            hint(WatchAppDatabase::class)
            WatchAppDatabase.Companion.getInstance(any())
        } returns appDatabase
    }

    @Test
    fun `Retry is returned when message failed to send`(): Unit = runBlocking {
        // Build worker
        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(
            context = context,
            inputData = workDataOf(EXTRA_WATCH_ID to watch.id.toString())
        ).build()

        // Mock message send failure
        coEvery { watchManagerRule.watchManager.sendMessage(any(), any(), any()) } returns false

        // Do work and check result
        val result = worker.doWork()
        expectThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `Success is returned when message sends successfully`(): Unit = runBlocking {
        // Build worker
        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(
            context = context,
            inputData = workDataOf(EXTRA_WATCH_ID to watch.id.toString())
        ).build()

        // Mock message send success
        coEvery { watchManagerRule.watchManager.sendMessage(any(), any(), any()) } returns true

        // Do work and check result
        val result = worker.doWork()
        expectThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `Validate cache message sends with valid data`(): Unit = runBlocking {
        // Build worker
        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(
            context = context,
            inputData = workDataOf(EXTRA_WATCH_ID to watch.id.toString())
        ).build()

        // Mock message send failure
        coEvery { watchManagerRule.watchManager.sendMessage(any(), any(), any()) } returns true

        // Do work and check result
        worker.doWork()
        coVerify { watchManagerRule.watchManager.sendMessage(watch, VALIDATE_CACHE, any()) }
    }

    @Test
    fun `Failure is returned when input data is missing`(): Unit = runBlocking {
        // Build worker
        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(
            context = context
        ).build()

        // Do work and check result
        val result = worker.doWork()
        expectThat(result).isEqualTo(ListenableWorker.Result.failure())
    }
}
