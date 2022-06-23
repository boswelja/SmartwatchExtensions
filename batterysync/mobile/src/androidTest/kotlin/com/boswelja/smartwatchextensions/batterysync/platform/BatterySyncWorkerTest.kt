package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.boswelja.smartwatchextensions.batterysync.platform.BatterySyncWorker.Companion.EXTRA_WATCH_ID
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.core.message.MessageClient
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncWorkerTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory { messageClient }
            }
        )
    }

    private lateinit var messageClient: MessageClient
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        messageClient = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun doWork_returnsFailureWhenUidMissing() = runTest {
        val worker = TestListenableWorkerBuilder<BatterySyncWorker>(context)
            .build()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun doWork_returnsSuccessWhenMessageSendSucceeds() = runTest {
        val uid = Watch.createUid("platform", "uid")
        val worker = TestListenableWorkerBuilder<BatterySyncWorker>(context)
            .setInputData(
                workDataOf(EXTRA_WATCH_ID to uid)
            )
            .build()

        // Mock sending success
        coEvery { messageClient.sendMessage(uid, any()) } returns true

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_returnsRetryWhenMessageSendFails() = runTest {
        val uid = Watch.createUid("platform", "uid")
        val worker = TestListenableWorkerBuilder<BatterySyncWorker>(context)
            .setInputData(
                workDataOf(EXTRA_WATCH_ID to uid)
            )
            .build()

        // Mock sending success
        coEvery { messageClient.sendMessage(uid, any()) } returns false

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
