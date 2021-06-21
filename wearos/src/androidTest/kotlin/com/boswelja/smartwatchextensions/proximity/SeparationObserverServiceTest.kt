package com.boswelja.smartwatchextensions.proximity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.boswelja.smartwatchextensions.expectTrueWithinTimeout
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.phoneconnectionmanager.ConnectionHelper
import com.boswelja.smartwatchextensions.phoneconnectionmanager.Status
import com.boswelja.smartwatchextensions.phoneconnectionmanager.phoneConnectionHelper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isTrue

class SeparationObserverServiceTest {

    @get:Rule
    val serviceTestRule = ServiceTestRule()

    @MockK private lateinit var connectionHelper: ConnectionHelper

    private lateinit var statusFlow: MutableStateFlow<Status>
    private lateinit var extensionSettingsStore: DataStore<ExtensionSettings>
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        notificationManager = context.getSystemService()!!

        // Set up phone status mock
        statusFlow = MutableStateFlow(Status.CONNECTED_NEARBY)
        every { connectionHelper.phoneStatus() } returns statusFlow
        mockkStatic(Context::phoneConnectionHelper)
        every { any<Context>().phoneConnectionHelper() } returns connectionHelper

        // Reset extension settings
        extensionSettingsStore = context.extensionSettingsStore
        runBlocking {
            extensionSettingsStore.updateData { it.copy(phoneSeparationNotis = true) }
        }

        // Start the service
        serviceTestRule.startService(Intent(context, SeparationObserverService::class.java))
    }

    @Test
    fun serviceStarted() {
        // Check foreground notification is shown
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == SeparationObserverService.FOREGROUND_NOTI_ID
            }
        }
    }

    @Test
    fun serviceStopsOnSettingDisabled() {
        // Disable extension
        runBlocking { extensionSettingsStore.updateData { it.copy(phoneSeparationNotis = false) } }

        // Check foreground notification clears
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.none {
                it.id == SeparationObserverService.FOREGROUND_NOTI_ID
            }
        }
    }

    @Test
    fun phoneStatusConnectedPostsNotification() {
        runBlocking { statusFlow.emit(Status.CONNECTED) }

        // Check there is a notification
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == SeparationObserverService.SEPARATION_NOTI_ID
            }
        }
    }

    @Test
    fun phoneStatusDisconnectedPostsNotification() {
        runBlocking { statusFlow.emit(Status.DISCONNECTED) }

        // Check there is a notification
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == SeparationObserverService.SEPARATION_NOTI_ID
            }
        }
    }

    @Test
    fun phoneStatusConnectedNearbyDoesntNotification() {
        runBlocking { statusFlow.emit(Status.CONNECTED_NEARBY) }

        // Give the service a chance to do something
        Thread.sleep(TIMEOUT)

        // Check there are no notifications
        expectThat(
            notificationManager.activeNotifications.none {
                it.id == SeparationObserverService.SEPARATION_NOTI_ID
            }
        ).isTrue()
    }

    companion object {
        const val TIMEOUT = 1000L
    }
}
