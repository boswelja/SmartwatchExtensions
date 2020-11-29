package com.boswelja.devicemanager.messages.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageHandler
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MessagesViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val message = Message(
        Message.Icon.HELP,
        "Test Message",
        "This is a test message",
        Message.Action.LAUNCH_CHANGELOG
    )

    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var messageDatabase: MessageDatabase
    private lateinit var messageHandler: MessageHandler

    @RelaxedMockK private lateinit var appUpdateManager: AppUpdateManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        messageDatabase = spyk(
            Room.inMemoryDatabaseBuilder(context, MessageDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        )
        messageHandler = spyk(
            MessageHandler(
                context,
                messageDatabase,
                mockk(relaxed = true),
                TestCoroutineScope()
            )
        )
        messagesViewModel = MessagesViewModel(
            ApplicationProvider.getApplicationContext(),
            messageDatabase,
            TestCoroutineDispatcher(),
            appUpdateManager,
            messageHandler
        )
    }

    @After
    fun tearDown() {
        messageDatabase.clearAllTables()
        messageDatabase.close()
    }

    @Test
    fun `Dismissing a message marks the message as deleted in database`() {
        val messageId = messageDatabase.messageDao().createMessage(message)

        val count = messageDatabase.messageDao().getActiveMessagesCount()
        count.getOrAwaitValue { assertThat(it).isEqualTo(1) }

        messagesViewModel.dismissMessage(messageId)
        count.getOrAwaitValue { assertThat(it).isEqualTo(0) }
        verify(exactly = 1) { messageHandler.dismissMessage(messageId) }
    }

    @Test
    fun `Restoring a message marks the message as not deleted in database`() {
        val messageId = messageDatabase.messageDao().createMessage(message)
        val count = messageDatabase.messageDao().getActiveMessagesCount()

        messagesViewModel.dismissMessage(messageId)
        count.getOrAwaitValue { assertThat(it).isEqualTo(0) }

        messagesViewModel.restoreMessage(messageId)
        count.getOrAwaitValue { assertThat(it).isEqualTo(1) }
        verify(exactly = 1) { messageHandler.restoreMessage(messageId) }
    }
}
