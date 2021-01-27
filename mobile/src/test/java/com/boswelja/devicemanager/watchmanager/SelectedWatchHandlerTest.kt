/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class SelectedWatchHandlerTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("an-id-1234", "Watch 1", WearOSConnectionManager.PLATFORM)

    private lateinit var database: WatchDatabase

    @MockK
    private lateinit var connectionManager: WearOSConnectionManager

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var selectedWatchHandler: SelectedWatchHandler
    private val coroutineScope = TestCoroutineScope()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { connectionManager.getWatchStatus(any(), any()) } returns Watch.Status.CONNECTED

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WatchDatabase::class.java
        ).allowMainThreadQueries().build()
        database.watchDao().add(dummyWatch)

        sharedPreferences = spyk(
            PreferenceManager
                .getDefaultSharedPreferences(
                    ApplicationProvider.getApplicationContext()
                )
        )

        selectedWatchHandler =
            SelectedWatchHandler(
                sharedPreferences,
                coroutineScope,
                connectionManager,
                database
            )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
        coroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun `Selecting a watch correctly updates selectedWatch LiveData`() {
        selectedWatchHandler.selectWatchById(dummyWatch.id)
        selectedWatchHandler.selectedWatch.getOrAwaitValue {
            assertThat(it).isEqualTo(dummyWatch)
        }
    }

    @Test(expected = TimeoutException::class)
    fun `Selecting an invalid watch does nothing`() {
        selectedWatchHandler.selectWatchById("")
        // This should throw TimeoutException as LiveData doesn't change
        selectedWatchHandler.selectedWatch.getOrAwaitValue()
    }

    @Test
    fun `Selecting a watch correctly updates SharedPreferences values`() {
        selectedWatchHandler.selectWatchById(dummyWatch.id)
        // Wait for the selected watch to be updated before running any checks
        selectedWatchHandler.selectedWatch.getOrAwaitValue()

        assertThat(sharedPreferences.getString("last_connected_id", ""))
            .isEqualTo(dummyWatch.id)
    }

    @Test
    fun `Selecting a watch updates status LiveData`() {
        selectedWatchHandler.selectWatchById(dummyWatch.id)
        selectedWatchHandler.status.getOrAwaitValue {
            assertThat(it).isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)
        }
    }

    @Test
    fun `Refreshing connected status updates status LiveData`() {
        // Set the initial value, and ensure it's correct
        selectedWatchHandler.selectWatchById(dummyWatch.id)
        selectedWatchHandler.status.getOrAwaitValue {
            assertThat(it).isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)
        }

        every { connectionManager.getWatchStatus(any(), any()) } returns Watch.Status.DISCONNECTED
        selectedWatchHandler.refreshStatus()
        selectedWatchHandler.status.getOrAwaitValue {
            assertThat(it).isEquivalentAccordingToCompareTo(Watch.Status.DISCONNECTED)
        }
    }
}
