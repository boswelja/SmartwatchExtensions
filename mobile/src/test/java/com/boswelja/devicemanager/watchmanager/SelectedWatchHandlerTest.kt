/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.os.Build
import android.os.Looper.getMainLooper
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

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

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    selectedWatchHandler =
        SelectedWatchHandler(
            ApplicationProvider.getApplicationContext(),
            sharedPreferences = sharedPreferences,
            database = dummyDatabase)

    // Emulate normal database behaviour, assuming dummyWatch exists.
    every { dummyDatabase.watchDao().get(dummyWatch.id) } returns dummyWatch
    every { dummyDatabase.watchDao().get("") } returns null
  }

  @Test
  fun `Selecting a watch correctly updates corresponding LiveData`() {
    selectedWatchHandler.selectWatchById(dummyWatch.id)
    shadowOf(getMainLooper()).idle()
    selectedWatchHandler.selectedWatch.getOrAwaitValue { assertThat(it).isEqualTo(dummyWatch) }

    selectedWatchHandler.selectWatchById("")
    shadowOf(getMainLooper()).idle()
    selectedWatchHandler.selectedWatch.getOrAwaitValue { assertThat(it).isNull() }
  }
}
