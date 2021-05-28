package com.boswelja.smartwatchextensions.messages.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEmpty

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class MessageHistoryViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MessageHistoryViewModel
    private lateinit var database: MessageDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MessageDatabase::class.java
        ).allowMainThreadQueries().build()
        viewModel = MessageHistoryViewModel(
            ApplicationProvider.getApplicationContext(),
            database
        )
    }

    @Test
    fun `clearMessageHistory calls database`(): Unit = runBlocking {
        // Manually add a dismissed message to the database
        val message = Message(
            Message.Icon.ERROR,
            "Title",
            "Text",
            deleted = true
        )
        database.messages().send(message)

        // Make the call
        viewModel.clearMessageHistory()

        // Check the message was deleted
        database.messages().dismissedMessages().take(1).collect {
            expectThat(it).isEmpty()
        }
    }
}
