/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BatterySyncViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var viewModel: BatterySyncViewModel

  @Before
  fun setUp() {
    viewModel = BatterySyncViewModel(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun `Toggling Battery Sync preference updates the corresponding LiveData`() {
    val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    sharedPreferences.edit(commit = true) {
      putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, true)
    }
    viewModel.batterySyncEnabled.getOrAwaitValue { assertThat(it).isTrue() }

    sharedPreferences.edit(commit = true) {
      putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
    }
    viewModel.batterySyncEnabled.getOrAwaitValue { assertThat(it).isFalse() }
  }
}
