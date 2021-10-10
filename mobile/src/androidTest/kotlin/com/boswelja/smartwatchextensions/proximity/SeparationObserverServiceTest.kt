package com.boswelja.smartwatchextensions.proximity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.boswelja.smartwatchextensions.expectTrueWithinTimeout
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.smartwatchextensions.watchmanager.item.BoolSetting
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class SeparationObserverServiceTest {

    @get:Rule
    val serviceTestRule = ServiceTestRule()

    private val watch = Watch("watch", "id", "platform")
    private val watchStatusFlow = MutableStateFlow(ConnectionMode.Disconnected)

    @MockK private lateinit var watchManager: WatchManager

    private lateinit var settingsDatabase: WatchSettingsDatabase
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        notificationManager = context.getSystemService()!!

        // Set up settings database
        settingsDatabase = Room.inMemoryDatabaseBuilder(
            context, WatchSettingsDatabase::class.java
        ).build()
        runBlocking {
            settingsDatabase.boolSettings().update(
                BoolSetting(
                    watch.uid, WATCH_SEPARATION_NOTI_KEY, true
                )
            )
        }

        // Mock WatchManager
        every { watchManager.getStatusFor(watch) } returns watchStatusFlow
        every { watchManager.getWatchById(watch.uid) } returns flow { emit(watch) }
        every { watchManager.settingsDatabase } returns settingsDatabase
        mockkObject(WatchManager.Companion)
        every { WatchManager.Companion.getInstance(any()) } returns watchManager

        // Start service
        serviceTestRule.startService(Intent(context, SeparationObserverService::class.java))
    }

    @Test
    fun serviceStartedSuccessfully() {
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == SeparationObserverService.FOREGROUND_NOTI_ID
            }
        }
    }

    @Test
    fun statusConnectedNearbyPostsNoNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Bluetooth) }

        // We wait here since we want to give a fair chunk of time for the service to try anything
        Thread.sleep(TIMEOUT)

        val hasPostedNoti = notificationManager.activeNotifications.none {
            it.id == watch.uid.hashCode()
        }

        expectThat(hasPostedNoti).isTrue()
    }

    @Test
    fun statusConnectedPostsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Internet) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.uid.hashCode()
            }
        }
    }

    @Test
    fun statusDisconnectedPostsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Disconnected) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.uid.hashCode()
            }
        }
    }

    @Test
    fun statusConnectedNearbyCancelsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Disconnected) }

        // Check notification was posted
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.uid.hashCode()
            }
        }

        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Bluetooth) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.none {
                it.id == watch.uid.hashCode()
            }
        }
    }

    @Test
    fun statusNotConnectedNearbyDoesntRepeatNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(ConnectionMode.Disconnected) }

        // Check notification was posted
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.uid.hashCode()
            }
        }

        // Get the notification time
        val notifyTime = notificationManager.activeNotifications
            .first { it.id == watch.uid.hashCode() }
            .postTime

        // Set status so that notification would be sent again
        runBlocking { watchStatusFlow.emit(ConnectionMode.Internet) }

        // Give the service a chance to notify again
        Thread.sleep(TIMEOUT)

        // Get the notification time again
        val secondNotifyTime = notificationManager.activeNotifications
            .first { it.id == watch.uid.hashCode() }
            .postTime

        // Ensure notification times were the same
        expectThat(secondNotifyTime).isEqualTo(notifyTime)
    }

    companion object {
        const val TIMEOUT = 500L
    }
}
