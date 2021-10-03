package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.app.NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class UtilsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        mockkConstructor(NotificationManager::class)
        context = spyk(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `dndState emits the correct value immediately`(): Unit = runBlocking {
        // Check true
        every {
            anyConstructed<NotificationManager>().currentInterruptionFilter
        } returns NotificationManager.INTERRUPTION_FILTER_NONE
        expectThat(withTimeout(COROUTINE_TIMEOUT) { context.dndState().first() }).isTrue()

        // Check false
        every {
            anyConstructed<NotificationManager>().currentInterruptionFilter
        } returns NotificationManager.INTERRUPTION_FILTER_ALL
        expectThat(withTimeout(COROUTINE_TIMEOUT) { context.dndState().first() }).isFalse()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `dndState updates on value changed`(): Unit = runBlocking {
        every {
            anyConstructed<NotificationManager>().currentInterruptionFilter
        } returns NotificationManager.INTERRUPTION_FILTER_NONE

        // Get a reference to the broadcast receiver
        val receiverFlow: MutableSharedFlow<BroadcastReceiver?> = MutableSharedFlow(replay = 1)
        every { context.registerReceiver(any(), any()) } answers {
            expectThat(receiverFlow.tryEmit(firstArg() as BroadcastReceiver)).isTrue()
            null
        }
        every { context.unregisterReceiver(any()) } returns Unit

        val results = mutableListOf<Boolean>()
        val deferred = async {
            withTimeoutOrNull(COROUTINE_TIMEOUT) {
                context.dndState().take(2).collect { results.add(it) }
            }
        }

        // Set DnD enabled
        every {
            anyConstructed<NotificationManager>().currentInterruptionFilter
        } returns NotificationManager.INTERRUPTION_FILTER_PRIORITY

        val receiver = withTimeoutOrNull(COROUTINE_TIMEOUT) { receiverFlow.first() }
        expectThat(receiver).isNotNull()

        // Emulate update received
        receiver!!.onReceive(context, Intent(ACTION_INTERRUPTION_FILTER_CHANGED))

        // Wait for our Flow to finish collecting
        deferred.await()

        // Check result
        expectThat(results.last()).isTrue()
    }

    companion object {
        private const val COROUTINE_TIMEOUT: Long = 2000
    }
}
