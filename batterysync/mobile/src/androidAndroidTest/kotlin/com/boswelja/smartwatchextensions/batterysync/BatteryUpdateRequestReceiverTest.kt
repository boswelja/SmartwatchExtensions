package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.message.MessageClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryUpdateRequestReceiverTest {

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
        messageClient = mockk()
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onMessageReceived_ignoresIncorrectPaths() = runTest {
        val receiver = BatteryUpdateRequestReceiver()
        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                "uid",
                "some-path",
                null
            )
        )
        coVerify(inverse = true) { messageClient.sendMessage(any(), any()) }
    }

    @Test
    fun onMessageReceived_sendsBatteryUpdate() = runTest {
        coEvery { messageClient.sendMessage(any(), any()) } returns true

        val uid = Watch.createUid("platform", "uid")
        val receiver = BatteryUpdateRequestReceiver()
        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                uid,
                REQUEST_BATTERY_UPDATE_PATH,
                null
            )
        )

        coVerify {
            messageClient.sendMessage(
                uid,
                coMatch {
                    BatteryStatsSerializer.deserialize(it.data)
                    true // If we get this far deserialization was successful
                }
            )
        }
    }
}
