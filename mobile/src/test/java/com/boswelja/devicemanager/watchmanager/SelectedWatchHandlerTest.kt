/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SelectedWatchHandlerTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("an-id-1234", "Watch 1", null)

    @RelaxedMockK private lateinit var dummyDatabase: WatchDatabase

    @SpyK
    private var sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    private lateinit var selectedWatchHandler: SelectedWatchHandler
    private val coroutineScope = TestCoroutineScope()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Emulate normal database behaviour, assuming dummyWatch exists.
        every { dummyDatabase.watchDao().get(dummyWatch.id) } returns dummyWatch
        every { dummyDatabase.watchDao().get("") } returns null

        selectedWatchHandler =
            SelectedWatchHandler(
                ApplicationProvider.getApplicationContext(),
                sharedPreferences = sharedPreferences,
                coroutineScope = coroutineScope,
                database = dummyDatabase
            )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
        coroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun `Selecting a watch correctly updates corresponding LiveData`(): Unit =
        coroutineScope.runBlockingTest {
            selectedWatchHandler.selectWatchById(dummyWatch.id)
            selectedWatchHandler.selectedWatch.getOrAwaitValue {
                assertThat(it).isEqualTo(dummyWatch)
            }

            selectedWatchHandler.selectWatchById("")
            selectedWatchHandler.selectedWatch.getOrAwaitValue { assertThat(it).isNull() }
        }

    @Test
    fun `Selecting a watch correctly updates SharedPreferences values`(): Unit =
        coroutineScope.runBlockingTest {
            selectedWatchHandler.selectWatchById(dummyWatch.id)
            assertThat(sharedPreferences.getString("last_connected_id", ""))
                .isEqualTo(dummyWatch.id)

            selectedWatchHandler.selectWatchById("")
            assertThat(sharedPreferences.getString("last_connected_id", "")).isEmpty()
        }
}
