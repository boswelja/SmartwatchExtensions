package com.boswelja.smartwatchextensions.proximity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.expectTrueWithinTimeout
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.smartwatchextensions.watchmanager.item.BoolSetting
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.discovery.Status
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import java.util.UUID
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

    private val watch = Watch(UUID.randomUUID(), "watch", "id", "platform")
    private val watchStatusFlow = MutableStateFlow(Status.CONNECTING)

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
                    watch.id, PreferenceKey.WATCH_SEPARATION_NOTI_KEY, true
                )
            )
        }

        // Mock WatchManager
        every { watchManager.getStatusFor(watch) } returns watchStatusFlow
        every { watchManager.getWatchById(watch.id) } returns flow { emit(watch) }
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
        runBlocking { watchStatusFlow.emit(Status.CONNECTED_NEARBY) }

        // We wait here since we want to give a fair chunk of time for the service to try anything
        Thread.sleep(TIMEOUT)

        val hasPostedNoti = notificationManager.activeNotifications.none {
            it.id == watch.id.hashCode()
        }

        expectThat(hasPostedNoti).isTrue()
    }

    @Test
    fun statusConnectedPostsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(Status.CONNECTED) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.id.hashCode()
            }
        }
    }

    @Test
    fun statusDisconnectedPostsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(Status.DISCONNECTED) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.id.hashCode()
            }
        }
    }

    @Test
    fun statusConnectedNearbyCancelsNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(Status.CONNECTED) }

        // Check notification was posted
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.id.hashCode()
            }
        }

        // Set the status
        runBlocking { watchStatusFlow.emit(Status.CONNECTED_NEARBY) }

        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.none {
                it.id == watch.id.hashCode()
            }
        }
    }

    @Test
    fun statusNotConnectedNearbyDoesntRepeatNotification() {
        // Set the status
        runBlocking { watchStatusFlow.emit(Status.CONNECTED) }

        // Check notification was posted
        expectTrueWithinTimeout(TIMEOUT) {
            notificationManager.activeNotifications.any {
                it.id == watch.id.hashCode()
            }
        }

        // Get the notification time
        val notifyTime = notificationManager.activeNotifications
            .first { it.id == watch.id.hashCode() }
            .postTime

        // Set status so that notification would be sent again
        runBlocking { watchStatusFlow.emit(Status.DISCONNECTED) }

        // Give the service a chance to notify again
        Thread.sleep(TIMEOUT)

        // Get the notification time again
        val secondNotifyTime = notificationManager.activeNotifications
            .first { it.id == watch.id.hashCode() }
            .postTime

        // Ensure notification times were the same
        expectThat(secondNotifyTime).isEqualTo(notifyTime)
    }

    companion object {
        const val TIMEOUT = 500L
    }
}
