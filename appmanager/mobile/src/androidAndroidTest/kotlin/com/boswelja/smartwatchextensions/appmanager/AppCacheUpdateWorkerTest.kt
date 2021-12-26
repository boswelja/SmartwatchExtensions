package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.boswelja.smartwatchextensions.appmanager.AppCacheUpdateWorker.Companion.EXTRA_WATCH_ID
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.core.message.MessageClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AppCacheUpdateWorkerTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory { messageClient }
                factory { appRepository }
            }
        )
    }

    private lateinit var messageClient: MessageClient
    private lateinit var appRepository: WatchAppRepository

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        messageClient = mockk()
        appRepository = mockk()
    }

    @Test
    fun doWork_returnsSuccessWhenCacheSendSucceeds(): Unit = runBlocking {
        val watchUid = Watch.createUid("platform", "id")

        // Mock a successful send
        every { appRepository.getAppVersionsFor(any()) } returns flowOf(emptyList())
        coEvery { messageClient.sendMessage(any(), any()) } returns true

        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(context)
            .setInputData(
                workDataOf(
                    EXTRA_WATCH_ID to watchUid
                )
            )
            .build()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_returnsRetryWhenCacheSendFails(): Unit = runBlocking {
        val watchUid = Watch.createUid("platform", "id")

        // Mock a failure sending
        every { appRepository.getAppVersionsFor(any()) } returns flowOf(emptyList())
        coEvery { messageClient.sendMessage(any(), any()) } returns false

        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(context)
            .setInputData(
                workDataOf(
                    EXTRA_WATCH_ID to watchUid
                )
            )
            .build()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun doWork_throwsWhenNoIdProvided(): Unit = runBlocking {
        val worker = TestListenableWorkerBuilder<AppCacheUpdateWorker>(context)
            .build()
        assertFails {
            worker.doWork()
        }
    }
}
