package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_LIST
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_START
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class UtilsKtTest {

    private val phoneId = "phone-id"
    private val dummyApps = createDummyApps(0..10)

    @Test
    fun `sendAllApps sends messages in order`(): Unit = runBlocking {
        // Mock app loading
        mockkStatic(Context::getAllApps)
        every { any<Context>().getAllApps() } returns dummyApps

        // Mock message client
        val messageClient = mockk<MessageClient>()
        every {
            messageClient.sendMessage(any(), any(), any())
        } returns Tasks.forResult(1)

        // Make the call
        ApplicationProvider.getApplicationContext<Context>().sendAllApps(phoneId, messageClient)

        // Verify messages
        verifyOrder {
            messageClient.sendMessage(phoneId, APP_SENDING_START, null)
            dummyApps.forEach {
                messageClient.sendMessage(
                    phoneId, APP_LIST, App.ADAPTER.encode(it)
                )
            }
            messageClient.sendMessage(phoneId, APP_SENDING_COMPLETE, null)
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
