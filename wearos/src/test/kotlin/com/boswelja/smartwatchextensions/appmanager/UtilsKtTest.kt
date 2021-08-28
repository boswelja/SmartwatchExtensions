package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.AppList
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_LIST
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_START
import com.boswelja.watchconnection.common.message.ByteArrayMessage
import com.boswelja.watchconnection.common.message.serialized.TypedMessage
import com.boswelja.watchconnection.core.Phone
import com.boswelja.watchconnection.wearos.discovery.DiscoveryClient
import com.boswelja.watchconnection.wearos.message.MessageClient
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

    private val phone = Phone("name", "id")
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
        coEvery { messageClient.sendMessage(any(), any()) } returns true

        // Make the call
        ApplicationProvider.getApplicationContext<Context>()
            .sendAllApps(messageClient, discoveryClient)

        // Verify messages
        coVerifyOrder {
            messageClient.sendMessage(phone, ByteArrayMessage(APP_SENDING_START))
            messageClient.sendMessage(phone, TypedMessage(APP_LIST, AppList(dummyApps)))
            messageClient.sendMessage(phone, ByteArrayMessage(APP_SENDING_COMPLETE))
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
