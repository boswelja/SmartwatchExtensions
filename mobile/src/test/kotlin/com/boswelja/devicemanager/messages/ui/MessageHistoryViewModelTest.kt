package com.boswelja.devicemanager.messages.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.messages.database.MessageDatabase
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class MessageHistoryViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var viewModel: MessageHistoryViewModel
    private lateinit var database: MessageDatabase

    @Before
    fun setUp() {
        database = spyk(
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MessageDatabase::class.java
            ).allowMainThreadQueries().build()
        )
        viewModel = MessageHistoryViewModel(
            ApplicationProvider.getApplicationContext(),
            database,
            testCoroutineDispatcher
        )
    }

    @Test
    fun `clearMessageHistory calls database`(): Unit = runBlocking {
        viewModel.clearMessageHistory()
        verify(exactly = 1) { database.clearMessageHistory() }
    }
}
