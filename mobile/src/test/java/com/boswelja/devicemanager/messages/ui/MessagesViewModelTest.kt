package com.boswelja.devicemanager.messages.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
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

    @Before
    fun setUp() {
        messageDatabase = spyk(
            Room.inMemoryDatabaseBuilder(context, MessageDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        )
        messagesViewModel = MessagesViewModel(
            ApplicationProvider.getApplicationContext(),
            messageDatabase,
            TestCoroutineDispatcher()
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
    }
}
