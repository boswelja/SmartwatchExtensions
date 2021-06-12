package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.WatchManagerTestRule
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.watchconnection.core.Watch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AppManagerViewModelTest {

    private val watch = Watch(UUID.randomUUID(), "", "", "")
    private val app = App(
        watchId = watch.id,
        icon = null,
        version = "",
        packageName = "",
        label = "",
        isSystemApp = false,
        hasLaunchActivity = true,
        isEnabled = true,
        installTime = 0,
        lastUpdateTime = 0,
        requestedPermissions = emptyList()
    )

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val watchManagerRule = WatchManagerTestRule()

    private lateinit var context: Application
    private lateinit var appDatabase: WatchAppDatabase

    private lateinit var viewModel: AppManagerViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Create dummy app database
        appDatabase = Room.inMemoryDatabaseBuilder(context, WatchAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        every {
            watchManagerRule.watchManager.selectedWatch
        } returns flow { emit(watch.toDbWatch()) }
        every {
            watchManagerRule.watchManager.registeredWatches
        } returns flow { emit(listOf(watch)) }
        every { watchManagerRule.watchManager.incomingMessages() } returns flow { }
        coEvery { watchManagerRule.watchManager.sendMessage(any(), any(), any()) } returns true

        viewModel = AppManagerViewModel(context, appDatabase, watchManagerRule.watchManager)
    }

    @Test
    fun `sendOpenRequest sends the correct message to the watch`(): Unit = runBlocking {
        viewModel.sendOpenRequest(app)

        coVerify {
            watchManagerRule.watchManager.sendMessage(
                watch, REQUEST_OPEN_PACKAGE, app.packageName.toByteArray(Charsets.UTF_8)
            )
        }
    }

    @Test
    fun `sendUninstallRequest sends the correct message to the watch`(): Unit = runBlocking {
        viewModel.sendUninstallRequest(app)

        coVerify {
            watchManagerRule.watchManager.sendMessage(
                watch, REQUEST_UNINSTALL_PACKAGE, app.packageName.toByteArray(Charsets.UTF_8)
            )
        }
    }

    @Test
    fun `validateCacheFor sends the correct message to the watch`(): Unit = runBlocking {
        viewModel.validateCache()

        coVerify {
            watchManagerRule.watchManager.sendMessage(
                watch, VALIDATE_CACHE, isNull(inverse = true)
            )
        }
    }
}
