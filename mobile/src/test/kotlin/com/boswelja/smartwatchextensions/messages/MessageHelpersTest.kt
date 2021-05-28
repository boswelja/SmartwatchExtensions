package com.boswelja.smartwatchextensions.messages

import android.app.NotificationManager
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class MessageHelpersTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context = spyk(InstrumentationRegistry.getInstrumentation().context)
    private val message = Message(
        Message.Icon.HELP,
        "Test Message",
        "This is a test message",
        Message.Action.LAUNCH_CHANGELOG
    )
    private lateinit var messageDatabase: MessageDatabase

    @RelaxedMockK
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        messageDatabase = Room.inMemoryDatabaseBuilder(context, MessageDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        messageDatabase.clearAllTables()
        messageDatabase.close()
    }

    @Test
    fun `Low priority message only posts the message to database`(): Unit = runBlocking {
        context.sendMessage(
            message,
            Priority.LOW,
            messageDatabase,
            notificationManager
        )
        verify(inverse = true) { notificationManager.notify(any(), any()) }
        val count = messageDatabase.messageDao().getActiveMessagesCount()
        count.getOrAwaitValue {
            expectThat(it).isEqualTo(1)
        }
    }

    @Test
    fun `High priority message posts to database and NotificationManager`(): Unit = runBlocking {
        context.sendMessage(
            message,
            Priority.HIGH,
            messageDatabase,
            notificationManager
        )
        verify(exactly = 1) { notificationManager.notify(any(), any()) }
        val count = messageDatabase.messageDao().getActiveMessagesCount()
        count.getOrAwaitValue {
            expectThat(it).isEqualTo(1)
        }
    }

    @Test
    fun `Notification channel is created`(): Unit = runBlocking {
        context.sendMessage(
            message,
            Priority.HIGH,
            messageDatabase,
            notificationManager
        )
        verify(exactly = 1) { notificationManager.createNotificationChannel(any()) }
    }
}
