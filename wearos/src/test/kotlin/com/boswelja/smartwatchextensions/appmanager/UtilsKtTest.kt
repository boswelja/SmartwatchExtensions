package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.watchconnection.common.Phone
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class UtilsKtTest {

    private val phone = Phone("id", "name")
    private val dummyApps = createDummyApps(0..10)

    @Test
    fun `sendAllApps sends messages in order`(): Unit = runBlocking {
        // Mock app loading
        mockkStatic(Context::getAllApps)
        every { any<Context>().getAllApps() } returns dummyApps

        // Mock discovery client
        val discoveryClient = mockk<DiscoveryClient>()
        coEvery { discoveryClient.pairedPhone() } returns phone

        // Mock message client
        val messageClient = mockk<MessageClient>()
        coEvery { messageClient.sendMessage(any(), any<Message<Any?>>()) } returns true

        // Make the call
        ApplicationProvider.getApplicationContext<Context>()
            .sendAllApps(messageClient, discoveryClient)

        // Verify messages
        coVerifyOrder {
            messageClient.sendMessage(phone, Message(APP_SENDING_START, null))
            messageClient.sendMessage(phone, Message(APP_LIST, AppList(dummyApps)))
            messageClient.sendMessage(phone, Message(APP_SENDING_COMPLETE, null))
        }
    }

    private fun createDummyApps(range: IntRange): List<App> {
        return range.map {
            App(
                version = "",
                packageName = "",
                label = "",
                isSystemApp = false,
                hasLaunchActivity = false,
                isEnabled = false,
                installTime = 0,
                lastUpdateTime = 0,
                requestedPermissions = emptyList()
            )
        }
    }
}
