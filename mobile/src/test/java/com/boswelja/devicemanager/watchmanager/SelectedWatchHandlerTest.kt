/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.os.Build
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.spyk
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

  val selectedWatchHandler =
      spyk(SelectedWatchHandler.get(ApplicationProvider.getApplicationContext()))

  val dummyWatch = Watch("an-id-1234", "Watch 1", null)

  @Before
  fun setUp() {
    every { selectedWatchHandler.database.watchDao().get(dummyWatch.id) } returns dummyWatch
    every { selectedWatchHandler.database.watchDao().get("") } returns null
  }

  @Test
  fun `Selecting a watch correctly updates corresponding LiveData`() {
    selectedWatchHandler.selectWatchById(dummyWatch.id)
    shadowOf(Looper.getMainLooper()).idle()
    selectedWatchHandler.selectedWatch.getOrAwaitValue { assertThat(it).isEqualTo(dummyWatch) }

    selectedWatchHandler.selectWatchById("")
    shadowOf(Looper.getMainLooper()).idle()
    selectedWatchHandler.selectedWatch.getOrAwaitValue { assertThat(it).isNull() }
  }
}
